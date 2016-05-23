package fi.csc.kapaVirtaAS;

/**
 * Created by joni on 10.5.2016.
 */
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class WSDLManipulator {

    private static final Logger log = LoggerFactory.getLogger(WSDLManipulator.class);
    private ASConfiguration conf;

    public WSDLManipulator(ASConfiguration conf){
        this.conf = conf;
    }

    public Element replaceAttribute(Element el, String attributeName, String newValue) {
        el.removeAttribute(attributeName);
        el.setAttribute(attributeName, newValue);
        return el;
    }
    
    public Element soapHeader(Element el, String message, String part, String use) {
        el.setAttribute("message", message);
        el.setAttribute("part", part);
        el.setAttribute("use", use);
        return el;
    }

    public void generateVirtaKapaWSDL() throws Exception{
        // Fetch current WSDL-file
        File inputFile = new File("target/wsdl/opiskelijatiedot.wsdl");
        DocumentBuilderFactory dbFactory
                = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.setXmlVersion("1.0");
        doc.getDocumentElement().normalize();

        // Manipulate WSDL to meet the requirements of xroad

        // Root element <wsdl:definitions> attribute manipulations
        Element root = doc.getDocumentElement();

        root.setAttribute("xmlns:"+conf.getXroadSchemaPrefixForWSDL(), conf.getXroadSchema());
        root.setAttribute("xmlns:"+conf.getXroadIdSchemaPrefixForWSDL(), conf.getXroadIdSchema());

        root = replaceAttribute(root, "xmlns:tns", conf.getAdapterServiceSchema());
        root = replaceAttribute(root, "targetNamespace", conf.getAdapterServiceSchema());

        // Schema elements <xs:schema> attribute manipulations
        NodeList schemas = root.getElementsByTagName("xs:schema");

        for(int i = 0; i < schemas.getLength(); ++i){
            Node schema = schemas.item(i);
            if(schema != null){
                NamedNodeMap schemaAttributes = schema.getAttributes();
                if(schemaAttributes != null && schemaAttributes.getNamedItem("xmlns:virtaluku") != null) {
                    schemaAttributes.getNamedItem("xmlns:virtaluku").setTextContent(conf.getAdapterServiceSchema());
                    if (schemaAttributes != null && schemaAttributes.getNamedItem("targetNamespace") != null) {
                        schemaAttributes.getNamedItem("targetNamespace").setTextContent(conf.getAdapterServiceSchema());
                    }
                    Element el = (Element) schema.appendChild(doc.createElement("xs:import"));
                    el.setAttribute("id", conf.getXroadSchemaPrefixForWSDL());
                    el.setAttribute("namespace", conf.getXroadSchema());
                    el.setAttribute("schemaLocation", conf.getXroadSchema());

                    // Remove Request part from xs:element -elements
                    NodeList elementsInSchema = schema.getChildNodes();
                    for(int j = 0; j < elementsInSchema.getLength(); ++j) {
                        Element el1 = (Element) elementsInSchema.item(j);
                        if (el1.getNodeName() == "xs:element") {
                            replaceAttribute(el1, "name", StringUtils.substringBefore(el1.getAttribute("name"), "Request"));
                        }

                    }
                }
            }
        }

        // Append xroad request headers
        Element xroadReqHeadersElement = doc.createElement("wsdl:message");
        xroadReqHeadersElement.setAttribute("name","requestheader");

        for(String xroadHeader : conf.getXroadHeaders()){
            Element reqHeader = doc.createElement("wsdl:part");
            reqHeader.setAttribute("name",xroadHeader);
            reqHeader.setAttribute("element", conf.getXroadSchemaPrefixForWSDL()+":"+xroadHeader);
            xroadReqHeadersElement.appendChild(reqHeader);
        }

        root.appendChild(xroadReqHeadersElement);


        NodeList childrenList = root.getChildNodes();
        for (int i = 0; i < childrenList.getLength(); ++i) {

            if (childrenList.item(i).getNodeName().contains("wsdl:binding")) {
                NodeList binding = childrenList.item(i).getChildNodes();
                for (int j = 0; j < binding.getLength(); ++j) {
                    if (binding.item(j).getNodeName().contains("wsdl:operation")) {
                        Element el1 = (Element) binding.item(j).appendChild(doc.createElement(conf.getXroadIdSchemaPrefixForWSDL()+":version"));
                        el1.setTextContent(conf.getVirtaVersionForXRoad());

                        for (Node child = binding.item(j).getFirstChild(); child != null; child = child.getNextSibling()) {

                            // Append xroad wsdl:binding operation headers
                            if (child.getNodeName().contains("wsdl:input") || child.getNodeName().contains("wsdl:output")) {
                                Element el = (Element) child;
                                for(String xroadHeader : conf.getXroadHeaders()) {
                                    el.appendChild(soapHeader(doc.createElement("soap:header"), "tns:requestheader", xroadHeader, "literal"));
                                }
                            }

                            if (child.getNodeName().contains("wsdl:input")) {
                                Element  el = (Element) child;
                                replaceAttribute(el, "name", StringUtils.substringBefore(el.getAttribute("name"), "Request"));
                            }
                        }
                    }
                }
            // Remove Request from wsdl:message > wsdl:part element so that can see element
            } else if (childrenList.item(i).getNodeName().contains("wsdl:message")
                    && childrenList.item(i).getAttributes().getNamedItem("name").getNodeValue().contains("Request")) {
                Element part = (Element) childrenList.item(i).getFirstChild().getNextSibling();
                replaceAttribute(part, "element", StringUtils.substringBefore(part.getAttribute("element"), "Request"));
            }

            // Change wsdl input names to meet XRoad standard
            if (childrenList.item(i).getNodeName().contains("wsdl:portType")) {
                NodeList binding = childrenList.item(i).getChildNodes();
                for (int j = 0; j < binding.getLength(); ++j) {
                    if (binding.item(j).getNodeName().contains("wsdl:operation")) {
                        for (Node child = binding.item(j).getFirstChild(); child != null; child = child.getNextSibling()) {
                            if (child.getNodeName().contains("wsdl:input")) {
                                Element el = (Element) child;
                                replaceAttribute(el, "name", StringUtils.substringBefore(el.getAttribute("name"), "Request"));
                            }
                        }
                    }
                }
            }

            // Append kapaVirtaAS service address
            if (childrenList.item(i).getNodeName().contains("wsdl:service")) {
                NodeList service= childrenList.item(i).getChildNodes();
                for (int j = 0; j < service.getLength(); ++j) {
                    if (service.item(j).getNodeName().contains("wsdl:port")) {
                        for (Node child = service.item(j).getFirstChild(); child != null; child = child.getNextSibling()) {
                            if (child.getNodeName().contains("soap:address")){
                                Element el = (Element) child;
                                replaceAttribute(el, "location", conf.getAdapterServiceSOAPURL());
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
