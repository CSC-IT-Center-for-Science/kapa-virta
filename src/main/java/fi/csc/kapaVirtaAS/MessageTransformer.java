/*
The MIT License (MIT)

Copyright (c) 2016 CSC - IT Center for Science

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package fi.csc.kapaVirtaAS;

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
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;

public class MessageTransformer {
    private static final Logger log = LoggerFactory.getLogger(VirtaClient.class);
    private ASConfiguration conf;
    private FaultMessageService faultMessageService;
    private String xroadSchemaPrefix;
    private String xroadIdSchemaPrefix;
    private String virtaServicePrefix = "xmlns:virtaluku";
    private Node xroadHeaderElement;
    private Node xroadRequestBody;

    public enum MessageDirection {
        XRoadToVirta,
        VirtaToXRoad;
    }

    public Node getXroadHeaderElement() {
        return xroadHeaderElement;
    }

    public MessageTransformer(ASConfiguration conf, FaultMessageService faultMessageService) {
        this.conf = conf;
        this.faultMessageService = faultMessageService;
    }

    public String transform(String message, MessageDirection direction) throws Exception {
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(stripXmlDefinition(message).getBytes())));
            doc.setXmlVersion("1.0");
            doc.normalizeDocument();
            Element root = doc.getDocumentElement();

            // Save XRoad schema prefix for response message
            if (direction == MessageDirection.XRoadToVirta) {
                NamedNodeMap rootAttributes = root.getAttributes();
                for (int i = 0; i < rootAttributes.getLength(); ++i) {
                    if (rootAttributes.item(i) != null &&
                            rootAttributes.item(i).getNodeName() != null &&
                            conf.getXroadSchema().equals(rootAttributes.item(i).getNodeValue())) {
                        xroadSchemaPrefix = rootAttributes.item(i).getNodeName();
                    }
                    if (rootAttributes.item(i) != null &&
                            rootAttributes.item(i).getNodeName() != null &&
                            conf.getXroadIdSchema().equals(rootAttributes.item(i).getNodeValue())) {
                        xroadIdSchemaPrefix = rootAttributes.item(i).getNodeName();
                    }
                }
            }
            // Add XRoad schemas with saved prefix to response message
            if (direction == MessageDirection.VirtaToXRoad) {
                if (xroadSchemaPrefix != null) {
                    root.setAttribute(xroadSchemaPrefix, conf.getXroadSchema());
                }
                if (xroadIdSchemaPrefix != null) {
                    root.setAttribute(xroadIdSchemaPrefix, conf.getXroadIdSchema());
                }
            }

            // Change tns schema
            NamedNodeMap attributes = root.getAttributes();
            for (int i = 0; i < attributes.getLength(); ++i) {
                Node attribute = attributes.item(i);

                if (direction == MessageDirection.XRoadToVirta &&
                        attribute.getNodeValue().contains(conf.getAdapterServiceSchema())) {
                    attribute.setNodeValue(conf.getVirtaServiceSchema());
                }
                if (direction == MessageDirection.VirtaToXRoad &&
                        attribute.getNodeValue().contains(conf.getVirtaServiceSchema())) {
                    attribute.setNodeValue(conf.getAdapterServiceSchema());
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
                        this.xroadHeaderElement = child.cloneNode(true);

                        //Strip XRoad SOAP-headers for Virta
                        //Replace SOAP-headers with same element without children
                        root.replaceChild(children.item(i).cloneNode(false), children.item(i));
                    }

                    if (direction == MessageDirection.VirtaToXRoad && this.xroadHeaderElement != null) {
                        //Append XRoad SOAP-headers back
                        for (int j = 0; j < this.xroadHeaderElement.getChildNodes().getLength(); ++j) {
                            child.appendChild(doc.importNode(this.xroadHeaderElement.getChildNodes().item(j), true));
                        }
                    }
                }

                //Change SOAP-body
                if (child != null && child.getNodeName().toLowerCase().contains("body")) {
                    for (Node bodyNode = child.getFirstChild(); bodyNode != null; bodyNode = bodyNode.getNextSibling()) {
                        //Change request appendix to operation input name
                        if (bodyNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element soapOperationElement = (Element) bodyNode;

                            //Virta gives malformed soap fault message. Need to parse it correct.
                            if (bodyNode.getNodeName().toLowerCase().contains("fault")) {
                                ((Element) soapOperationElement.getElementsByTagName("faultstring").item(0)).removeAttribute("xml:lang");
                            } else {

                                if (direction == MessageDirection.XRoadToVirta) {
                                    //Save XRoadRequestBody for response message
                                    xroadRequestBody = soapOperationElement.cloneNode(true);

                                    //Add postfix after SOAP-operation name
                                    //eg. Opintosuoritukset -> OpintosuorituksetRequest
                                    doc.renameNode(soapOperationElement, conf.getVirtaServiceSchema(), soapOperationElement.getTagName() + "Request");
                                }

                                if (direction == MessageDirection.VirtaToXRoad) {
                                    //Response part namespace change
                                    soapOperationElement.removeAttribute(virtaServicePrefix);
                                    soapOperationElement.setAttribute(virtaServicePrefix, conf.getAdapterServiceSchema());
                                }
                            }
                        }
                    }
                }
            }

            doc.normalizeDocument();
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(domSource, result);
            message = writer.toString();

            return stripXmlDefinition(message);
        }
        catch(Exception e){
            if(direction == MessageDirection.XRoadToVirta){
                log.error("Error in parsing request message.");
                log.error(e.toString());
                return stripXmlDefinition(faultMessageService.generateSOAPFault(message, faultMessageService.getReqValidFail(), this.xroadHeaderElement));
            }
            else {
                log.error("Error in parsing response message");
                log.error(e.toString());
                return stripXmlDefinition(faultMessageService.generateSOAPFault(message, faultMessageService.getResValidFail(), this.xroadHeaderElement));
            }
        }
    }

    private String stripXmlDefinition(String message) {
        //Remove xml definition element, if any
        //eg. <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        String sub = StringUtils.substringAfter(message, "?>");
        if(sub != null && sub != ""){
            return sub;
        }
        return message;
    }

}
