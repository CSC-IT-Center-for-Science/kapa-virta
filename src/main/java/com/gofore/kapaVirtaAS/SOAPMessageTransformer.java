package com.gofore.kapaVirtaAS;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

/**
 * Created by joni on 12.5.2016.
 */
public class SOAPMessageTransformer {
    private static final Logger log = LoggerFactory.getLogger(VirtaClient.class);
    private final String ASserviceURI = "http://test.x-road.virta.csc.fi/producer";
    private final String virtaServiceURI = "http://tietovaranto.csc.fi/luku";
    private final String xroadSchemaURI = "http://x-road.eu/xsd/xroad.xsd";
    private String xroadSchemaPrefix;
    private String virtaServicePrefix;
    private Node XRoadHeaderElement;
    private Node XRoadRequestBody;

    public enum MessageDirection {
        XRoadToVirta,
        VirtaToXRoad;
    }

    public SOAPMessageTransformer() {
    }

    public String transform(String message, MessageDirection direction) throws Exception {
        DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(stripXmlDefinition(message).getBytes())));
        doc.setXmlVersion("1.1");
        Element root = doc.getDocumentElement();

        // Save XRoad schema prefix for response message
        if (direction == MessageDirection.XRoadToVirta) {
            NamedNodeMap rootAttributes = root.getAttributes();
            for (int i = 0; i < rootAttributes.getLength(); ++i) {
                if (rootAttributes.item(i) != null &&
                        rootAttributes.item(i).getNodeName() != null &&
                        xroadSchemaURI.equals(rootAttributes.item(i).getNodeValue())) {
                    xroadSchemaPrefix = rootAttributes.item(i).getNodeName();
                }
            }
        }

        // Change tns schema
        NamedNodeMap attributes = root.getAttributes();
        for (int i = 0; i < attributes.getLength(); ++i) {
            Node attribute = attributes.item(i);

            if (direction == MessageDirection.XRoadToVirta &&
                    attribute.getNodeValue().contains(ASserviceURI)) {
                attribute.setNodeValue(virtaServiceURI);
            }
        }

        //There should be two children under the root node: header and body
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);

            //Change SOAP-headers
            if (child != null && child.getNodeName().toLowerCase().contains("header")) {
                if (direction == MessageDirection.XRoadToVirta) {
                    //Save XRoadHeaderElement for response message
                    this.XRoadHeaderElement = child.cloneNode(true);
                    //Replace SOAP-headers with same element without children
                    root.replaceChild(child.cloneNode(false), child);
                }

                if (direction == MessageDirection.VirtaToXRoad && this.XRoadHeaderElement != null) {
                    // Add XRoad schemas with saved prefix to response message
                    Element el = (Element) child;
                    el.setAttribute(xroadSchemaPrefix, xroadSchemaURI);

                    //Append XRoad SOAP-headers back
                    for (int j = 0; j < this.XRoadHeaderElement.getChildNodes().getLength(); ++j) {
                        child.appendChild(doc.importNode(this.XRoadHeaderElement.getChildNodes().item(j), true));
                    }
                }
            }

            //Change SOAP-body
            if (child != null && child.getNodeName().toLowerCase().contains("body")) {
                for (Node bodyNode = child.getFirstChild(); bodyNode != null; bodyNode = bodyNode.getNextSibling()) {
                    //Change request appendix to operation input name
                    if (bodyNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element soapOperationElement = (Element) bodyNode;
                        if (direction == MessageDirection.XRoadToVirta) {
                            //Add postfix after SOAP-operation name
                            //eg. Opintosuoritukset -> OpintosuorituksetRequest
                            doc.renameNode(soapOperationElement, virtaServiceURI, soapOperationElement.getTagName() + "Request");

                            //Save XRoadRequestBody for response message
                            XRoadRequestBody = soapOperationElement;
                        }

                        if (direction == MessageDirection.VirtaToXRoad) {
                            //Remove Request appendix to operation input name
                            //eg. OpintosuorituksetRequest -> Opintosuoritukset
                            String tagNameForXRoad = StringUtils.substringBefore(soapOperationElement.getTagName(), "Request");
                            //Rename tag with XRoad naming conventions
                            doc.renameNode(soapOperationElement, ASserviceURI, tagNameForXRoad);

                            //Save Virta service schema uri
                            virtaServicePrefix = StringUtils.substringBefore(soapOperationElement.getTagName(), ":");
                        }
                    }
                }
                //Append XRoadRequestBody to response message body
                if (direction == MessageDirection.VirtaToXRoad) {
                    child.appendChild(doc.importNode(XRoadRequestBody, true));
                    //doc.renameNode(child, virtaServiceURI, )
                }
            }
        }

        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(domSource, result);
        message = writer.toString();

        return stripXmlDefinition(message);
    }

    private String stripXmlDefinition(String message) {
        //Remove xml definition element, if any
        //eg. <?xml version="1.1" encoding="UTF-8" standalone="no"?>
        String sub = StringUtils.substringAfter(message, "?>");
        if(sub != null && sub != ""){
            return sub;
        }
        return message;
    }
}
