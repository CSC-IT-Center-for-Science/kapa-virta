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
import org.mockito.internal.exceptions.ExceptionIncludingMockitoWarnings;
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
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MessageTransformer {
    private static final Logger log = LoggerFactory.getLogger(VirtaClient.class);
    private ASConfiguration conf;
    private FaultMessageService faultMessageService;
    private String xroadSchemaPrefix;
    private String xroadIdSchemaPrefix;
    private String virtaServicePrefix = "xmlns:virtaluku";
    private Node xroadHeaderElement;

    public enum MessageDirection {
        XRoadToVirta,
        VirtaToXRoad
    }

    public Node getXroadHeaderElement() {
        return xroadHeaderElement;
    }

    public MessageTransformer(ASConfiguration conf, FaultMessageService faultMessageService) {
        this.conf = conf;
        this.faultMessageService = faultMessageService;
    }

    public String createAuthenticationString(String message){
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(stripXmlDefinition(message).getBytes())));
            doc.setXmlVersion("1.0");
            doc.normalizeDocument();

            Optional<NodeList> headerNodes = getChildByKeyword(nodeListToStream(doc.getDocumentElement().getChildNodes()), "header");
            Optional<NodeList> clientHeaders = headerNodes.map(nodeList -> getChildByKeyword(nodeListToStream(nodeList), "client")).orElse(Optional.empty());

            Number l = clientHeaders.get().getLength();
            l.toString();

            return clientHeaders.map(nodeList -> getNodeByKeyword(nodeListToStream(nodeList), "xRoadInstance").map(node -> node.getTextContent())).orElse(Optional.empty()).orElse("") + ":"
                + clientHeaders.map(nodeList -> getNodeByKeyword(nodeListToStream(nodeList), "memberClass").map(node -> node.getTextContent())).orElse(Optional.empty()).orElse("") + ":"
                + clientHeaders.map(nodeList -> getNodeByKeyword(nodeListToStream(nodeList), "memberCode").map(node -> node.getTextContent())).orElse(Optional.empty()).orElse("") + ":"
                + clientHeaders.map(nodeList -> getNodeByKeyword(nodeListToStream(nodeList), "subsystemCode").map(node -> node.getTextContent())).orElse(Optional.empty()).orElse("");
        }
        catch(Exception e) {
            log.error("Error in parsing authenticationstring");
            log.error(e.toString());
            return "";
        }
    }

    public String transform(String message, MessageDirection direction) throws Exception {
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(stripXmlDefinition(message).getBytes())));
            doc.setXmlVersion("1.0");
            doc.normalizeDocument();
            Element root = doc.getDocumentElement();

            if (direction == MessageDirection.XRoadToVirta) {
                // Save XRoad schema prefix for response message
                xroadSchemaPrefix = namedNodeMapToStream(root.getAttributes())
                        .filter(node -> node.getNodeValue().toLowerCase().contains(conf.getXroadSchema().toLowerCase()))
                        .findFirst().orElseThrow(() -> new DOMException(DOMException.NOT_FOUND_ERR, "Xroad schema prefix not found"))
                        .getNodeName();

                xroadIdSchemaPrefix = namedNodeMapToStream(root.getAttributes())
                        .filter(node -> node.getNodeValue().toLowerCase().contains(conf.getXroadIdSchema().toLowerCase()))
                        .findFirst().orElseThrow(() -> new DOMException(DOMException.NOT_FOUND_ERR, "XroadId schema prefix not found"))
                        .getNodeName();

                // Change tns schema
                getNodeByKeyword(namedNodeMapToStream(root.getAttributes()), conf.getAdapterServiceSchema()).map(attribute -> setNodeValueToNode(attribute,conf.getVirtaServiceSchema()));

                //There should be two children under the root node: header and body
                for (int i = 0; i < root.getChildNodes().getLength(); ++i) {
                    Node child = root.getChildNodes().item(i);
                    // Save soap-headers for reply message and remove child elements under soap-headers
                    if(child.getNodeName().toLowerCase().contains("header")){
                        this.xroadHeaderElement = child.cloneNode(true);
                        root.replaceChild(child.cloneNode(false), child);
                    }
                    // Change SOAP-body
                    else if(child.getNodeName().toLowerCase().contains("body")) {
                        for(int j = 0; j < child.getChildNodes().getLength(); ++j) {
                            if(child.getChildNodes().item(j).getNodeType() == Node.ELEMENT_NODE) {
                                doc.renameNode(child.getChildNodes().item(j), conf.getVirtaServiceSchema(),child.getChildNodes().item(j).getNodeName() + "Request");
                                break;
                            }
                        }

                    }
                }
            }
            if (direction == MessageDirection.VirtaToXRoad) {
                // Add XRoad schemas with saved prefix to response message
                root.setAttribute(xroadSchemaPrefix, conf.getXroadSchema());
                root.setAttribute(xroadIdSchemaPrefix, conf.getXroadIdSchema());

                // Change tns schema
                getNodeByKeyword(namedNodeMapToStream(root.getAttributes()), conf.getVirtaServiceSchema()).map(attribute -> setNodeValueToNode(attribute,conf.getAdapterServiceSchema()));

                // Change SOAP-headers
                Node headerNode = getNodeByKeyword(nodeListToStream(root.getChildNodes()), "header").get();
                for (int i = 0; i < this.xroadHeaderElement.getChildNodes().getLength(); ++i) {
                    headerNode.appendChild(doc.importNode(this.xroadHeaderElement.getChildNodes().item(i), true));
                }

                // Change SOAP-body
                getNodeByKeyword(nodeListToStream(root.getChildNodes()),"body")
                        .map(bodyNode -> removeAttribureFromElement(nodeToElement(bodyNode),virtaServicePrefix))
                        .map(bodyNode -> setAttributeToElement(nodeToElement(bodyNode), virtaServicePrefix, conf.getAdapterServiceSchema()));

                //Virta gives malformed soap fault message. Need to parse it correct.
                getNodeByKeyword(nodeListToStream(root.getChildNodes()),"body")
                        .map(bodyNode -> nodeListToStream(bodyNode.getChildNodes()))
                        .map(nodesInBodyStream -> getNodeByKeyword(nodesInBodyStream, "fault")
                            .map(faultNode -> removeAttribureFromElement(nodeToElement(nodeToElement(faultNode).getElementsByTagName("faultstring").item(0)),"xml:lang")));
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
            if(direction == MessageDirection.XRoadToVirta) {
                log.error("Error in parsing request message.");
                throw e;
            }
            else {
                log.error("Error in parsing response message");
                log.error(e.toString());
                return stripXmlDefinition(faultMessageService.generateSOAPFault(message, faultMessageService.getResValidFail(), this.xroadHeaderElement));
            }
        }
    }

    private Node setNodeValueToNode(Node n, String value) {
        n.setNodeValue(value);
        return n;
    }

    private Element setAttributeToElement(Element e, String var1, String var2) {
        e.setAttribute(var1, var2);
        return e;
    }

    private Element removeAttribureFromElement(Element e, String attribute) {
        e.removeAttribute(attribute);
        return e;
    }

    private Element nodeToElement(Node node) {
        return (Element) node;
    }

    private Stream<Node> namedNodeMapToStream(NamedNodeMap namedNodeMap) {
        return IntStream.range(0, namedNodeMap.getLength()).mapToObj(namedNodeMap::item);
    }

    private Stream<Node> nodeListToStream(NodeList nodelist) {
        return IntStream.range(0, nodelist.getLength()).mapToObj(nodelist::item).filter(item -> item.getNodeType() == Node.ELEMENT_NODE);
    }

    private Optional<Node> getNodeByKeyword(Stream<Node> nodeStream, String keyword) {
        return nodeStream
                .filter(x -> x.getNodeName().toLowerCase().contains(keyword.toLowerCase()))
                .findFirst();
    }

    private Optional<NodeList> getChildByKeyword(Stream<Node> nodeStream, String keyword) {
        return nodeStream
                .filter(x -> x.getNodeName().toLowerCase().contains(keyword.toLowerCase()))
                .findFirst()
                .map(x -> x.getChildNodes());
    }

    //Removes xml definition element, if any
    //eg. <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    private String stripXmlDefinition(String message) {

        String sub = StringUtils.substringAfter(message, "?>");
        if(sub != null && sub != "") {
            return sub;
        }
        return message;
    }
}
