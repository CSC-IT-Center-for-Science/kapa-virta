package kapaVirtaAS;

/**
 * Created by joni on 10.5.2016.
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

public class WSDLManipulator {

    private static final Logger log = LoggerFactory.getLogger(WSDLManipulator.class);
    private static final String serviceName = "http://test.x-road.virta.csc.fi/producer";
    private static final String xroadSchema = "xrd";
    private static final String[] xroadReqHeaders = {"client", "service", "userId", "id", "issue", "protocolVersion"};

    public static Element replaceAttribute(Element el, String name) {
        el.removeAttribute("xmlns:tns");
        el.removeAttribute("targetNamespace");
        el.setAttribute("xmlns:tns", serviceName);
        el.setAttribute("targetNamespace", serviceName);
        return el;
    }
    
    public static Element soapHeader(Element el, String message, String part, String use) {
        el.setAttribute("message", message);
        el.setAttribute("part", part);
        el.setAttribute("use", use);
        return el;
    }

    public static void generateVirtaKapaWSDL() throws Exception{
        // Fetch current WSDL-file
        File inputFile = new File("target/wsdl/opiskelijatiedot.wsdl");
        DocumentBuilderFactory dbFactory
                = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();

        // Manipulate WSDL to meet the requirements of xroad

        // Root element <wsdl:definitions> attribute manipulations
        Element root = doc.getDocumentElement();

        root.setAttribute("xmlns:"+xroadSchema, "http://x-road.eu/xsd/xroad.xsd");
        root.setAttribute("xmlns:id", "http://x-road.eu/xsd/identifiers");

        root = replaceAttribute(root, "xmlns:tns");

        // Schema elements <xs:schema> attribute manipulations
        NodeList schemas = root.getElementsByTagName("xs:schema");
        for(int i = 0; i < schemas.getLength(); ++i){
            Node schema = schemas.item(i);
            if(schema != null){
                NamedNodeMap schemaAttributes = schema.getAttributes();
                if(schemaAttributes != null && schemaAttributes.getNamedItem("xmlns:virtaluku") != null){
                    schemaAttributes.getNamedItem("xmlns:virtaluku").setTextContent(serviceName);
                    if(schemaAttributes != null && schemaAttributes.getNamedItem("targetNamespace") != null){
                        schemaAttributes.getNamedItem("targetNamespace").setTextContent(serviceName);
                    }
                }
            }
        }

        // Append xroad requestheaders

        Element xroadReqHeadersElement = doc.createElement("wsdl:message");
        xroadReqHeadersElement.setAttribute("name","requestheader");

        for(String xroadReqHeader : xroadReqHeaders){
            Element reqHeader = doc.createElement("wsdl:part");
            reqHeader.setAttribute("name",xroadReqHeader);
            reqHeader.setAttribute("element", xroadSchema+":"+xroadReqHeader);
            xroadReqHeadersElement.appendChild(reqHeader);
        }

        root.appendChild(xroadReqHeadersElement);

        // Append xroad wsdl:binding operation headers
        NodeList childrenList = root.getChildNodes();
        for (int i = 0; i < childrenList.getLength(); ++i) {
            if (childrenList.item(i).getNodeName().contains("wsdl:binding")) {
                NodeList binding = childrenList.item(i).getChildNodes();
                for (int j = 0; j < binding.getLength(); ++j) {
                    if (binding.item(j).getNodeName().contains("wsdl:operation")) {
                        for (Node child = binding.item(j).getFirstChild(); child != null; child = child.getNextSibling()) {
                            if (child.getNodeName().contains("wsdl:input") || child.getNodeName().contains("wsdl:output")) {
                                Element el = (Element) child;
                                el.appendChild(soapHeader(doc.createElement("soap:header"), "tns:requesheader", "client", "literal"));
                                el.appendChild(soapHeader(doc.createElement("soap:header"), "tns:requesheader", "service", "literal"));
                                el.appendChild(soapHeader(doc.createElement("soap:header"), "tns:requesheader", "userId", "literal"));
                                el.appendChild(soapHeader(doc.createElement("soap:header"), "tns:requesheader", "id", "literal"));
                                el.appendChild(soapHeader(doc.createElement("soap:header"), "tns:requesheader", "issue", "literal"));
                                el.appendChild(soapHeader(doc.createElement("soap:header"), "tns:requesheader", "protocolVersion", "literal"));
                            }
                        }
                    }
                }
            }
        }


        // Write manipulated WSDL to file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File("static/wsdl/generatedKapavirta.wsdl"));
        transformer.transform(source, result);
    }
}
