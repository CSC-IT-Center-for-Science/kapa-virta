package kapaVirtaAS;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.common.i18n.Message;
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
import java.util.Arrays;

/**
 * Created by joni on 12.5.2016.
 */
public class SOAPMessageTransformer {
    private static final Logger log = LoggerFactory.getLogger(VirtaClient.class);
    private final String serviceName = "http://test.x-road.virta.csc.fi/producer";
    private final String virtaServiceName = "http://tietovaranto.csc.fi/luku";
    private final String xroadSchemaURI = "http://x-road.eu/xsd/xroad.xsd";
    private final String xroadIdSchemaURI = "http://x-road.eu/xsd/identifiers";
    private String xroadSchemaAttribute;
    private String xroadIdSchemaAttribute;
    private Node XRoadHeaderElement;


    public enum MessageDirection{
        XRoadToVirta,
        VirtaToXRoad;
    }

    public SOAPMessageTransformer() {
    }

    public String transform(String message, MessageDirection direction) throws Exception{
        //Remove xml definition element, if any
        String sub = StringUtils.substringAfter(message, "?>");
        if(sub != null && sub != ""){
            message = sub;
        }

        DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(message.getBytes())));
        Element root = doc.getDocumentElement();

        // Get XRoad schemas
        if(direction == MessageDirection.XRoadToVirta) {
            NamedNodeMap rootAttributes = root.getAttributes();
            for(int i = 0; i < rootAttributes.getLength(); ++i){
                if(rootAttributes.item(i).getNodeValue() == xroadSchemaURI){
                    String name = rootAttributes.item(i).getNodeName();
                    name.toString();
                }
                if(rootAttributes.item(i).getNodeValue() == xroadIdSchemaURI) {
                    String name = rootAttributes.item(i).getNodeName();
                    name.toString();
                }
            }
            root.setAttribute(xroadIdSchemaAttribute, xroadIdSchemaURI);
        }

        // Add XRoad schemas
        if(direction == MessageDirection.VirtaToXRoad) {
            root.setAttribute(xroadSchemaAttribute, xroadSchemaURI);
            root.setAttribute(xroadIdSchemaAttribute, xroadIdSchemaURI);
        }

        // Change tns schema
        NamedNodeMap attributes = root.getAttributes();
        for(int i = 0; i < attributes.getLength(); ++i) {
            Node attribute = attributes.item(i);

            if(direction == MessageDirection.XRoadToVirta &&
                    attribute.getNodeValue().contains(serviceName)){
                attribute.setNodeValue(virtaServiceName);
            }

            if(direction == MessageDirection.VirtaToXRoad &&
                    attribute.getNodeValue().contains(virtaServiceName)){
                attribute.setNodeValue(serviceName);
            }
        }

        // Change SOAP-headers
        // Change SOAP-operation input name in SOAP-body

        //There should be two children under the root node: header and body
        NodeList children = root.getChildNodes();
        for(int i = 0; i < children.getLength(); ++i){
            Node child = children.item(i);

            //Change SOAP-headers
            if(child != null && child.getNodeName().toLowerCase().contains("header")){

                if(direction == MessageDirection.XRoadToVirta) {
                    this.XRoadHeaderElement = child.cloneNode(true);
                    //Replace SOAP-headers with same element without children
                    root.replaceChild(child.cloneNode(false), child);
                }

                if(direction == MessageDirection.VirtaToXRoad && this.XRoadHeaderElement != null) {
                    //Append XRoad SOAP-headers back
                    for(int j = 0; j < this.XRoadHeaderElement.getChildNodes().getLength(); ++j){
                        child.appendChild(doc.importNode(this.XRoadHeaderElement.getChildNodes().item(j),true));
                    }
                }
            }

            //Change request appendix to operation input name
            if(child != null && child.getNodeName().toLowerCase().contains("body")){
                Node soapOperationNode = child.getFirstChild();
                Element soapOperationElement = null;
                if(soapOperationNode == null || soapOperationNode.getNodeType() != Node.ELEMENT_NODE) {
                    soapOperationNode = child.getFirstChild().getNextSibling();
                }

                if(soapOperationNode.getNodeType() == Node.ELEMENT_NODE){
                    soapOperationElement = (Element) soapOperationNode;
                }
                else{
                    child.toString();
                }

                if(direction == MessageDirection.XRoadToVirta){
                    //Add postfix after SOAP-operation name
                    //eg. Opintosuoritukset -> OpintosuorituksetRequest
                    doc.renameNode(soapOperationElement, virtaServiceName, soapOperationElement.getTagName()+"Request");
                }

                if(direction == MessageDirection.VirtaToXRoad){
                    //Remove Request appendix to operation input name
                    //eg. OpintosuorituksetRequest -> Opintosuoritukset
                    String tagNameForXRoad =  StringUtils.substringBefore(soapOperationElement.getTagName(),"Request");
                    doc.renameNode(soapOperationElement, serviceName, tagNameForXRoad);
                }
            }
        }

        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(domSource, result);
        message = writer.toString();

        return message;
    }
}
