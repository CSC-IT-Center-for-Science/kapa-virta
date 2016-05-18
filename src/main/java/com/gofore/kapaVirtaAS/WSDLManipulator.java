package com.gofore.kapaVirtaAS;

/**
 * Created by joni on 10.5.2016.
 */
import org.apache.commons.lang.StringUtils;
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
    private static final String serviceName = "http://test.x-road.virta.csc.fi/producer";
    private static final String xroadSchema = "xrd";
    private static final String[] xroadReqHeaders = {"client", "service", "userId", "id", "issue", "protocolVersion"};
    private static final String soapServiceURL = "http://localhost:8080/ws";

    public static Element replaceAttribute(Element el, String attributeName, String newValue) {
        el.removeAttribute(attributeName);
        el.setAttribute(attributeName, newValue);
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
        doc.setXmlVersion("1.0");
        doc.getDocumentElement().normalize();

        // Manipulate WSDL to meet the requirements of xroad

        // Root element <wsdl:definitions> attribute manipulations
        Element root = doc.getDocumentElement();

        root.setAttribute("xmlns:"+xroadSchema, "http://x-road.eu/xsd/xroad.xsd");
        root.setAttribute("xmlns:id", "http://x-road.eu/xsd/identifiers");

        root = replaceAttribute(root, "xmlns:tns", serviceName);
        root = replaceAttribute(root, "targetNamespace", serviceName);

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


        NodeList childrenList = root.getChildNodes();
        for (int i = 0; i < childrenList.getLength(); ++i) {

            if (childrenList.item(i).getNodeName().contains("wsdl:binding")) {
                NodeList binding = childrenList.item(i).getChildNodes();
                for (int j = 0; j < binding.getLength(); ++j) {
                    if (binding.item(j).getNodeName().contains("wsdl:operation")) {
                        Element el1 = (Element) binding.item(j).appendChild(doc.createElement("id:version"));
                        el1.setTextContent("v1");

                        for (Node child = binding.item(j).getFirstChild(); child != null; child = child.getNextSibling()) {

                            // Append xroad wsdl:binding operation headers
                            if (child.getNodeName().contains("wsdl:input") || child.getNodeName().contains("wsdl:output")) {
                                Element el = (Element) child;
                                el.appendChild(soapHeader(doc.createElement("soap:header"), "tns:requestheader", "client", "literal"));
                                el.appendChild(soapHeader(doc.createElement("soap:header"), "tns:requestheader", "service", "literal"));
                                el.appendChild(soapHeader(doc.createElement("soap:header"), "tns:requestheader", "userId", "literal"));
                                el.appendChild(soapHeader(doc.createElement("soap:header"), "tns:requestheader", "id", "literal"));
                                el.appendChild(soapHeader(doc.createElement("soap:header"), "tns:requestheader", "issue", "literal"));
                                el.appendChild(soapHeader(doc.createElement("soap:header"), "tns:requestheader", "protocolVersion", "literal"));
                            }

                            if (child.getNodeName().contains("wsdl:input")) {
                                Element  el = (Element) child;
                                replaceAttribute(el, "name", StringUtils.substringBefore(el.getAttribute("name"), "Request"));
                            }
                        }
                    }
                }
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
                                replaceAttribute(el, "location", soapServiceURL);
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
