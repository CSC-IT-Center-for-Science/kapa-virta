package kapaVirtaAS.VirtaClient;

/**
 * Created by joni on 9.5.2016.
 */
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

public class VirtaClient {

    private static final Logger log = LoggerFactory.getLogger(VirtaClient.class);
    private static final String testSoapToVirta = "<x:Envelope xmlns:x=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:luk=\"http://tietovaranto.csc.fi/luku\"><x:Header/><x:Body><luk:LukukausiIlmoittautumisetRequest><luk:Kutsuja><luk:jarjestelma/><luk:tunnus/><luk:avain>salaisuus</luk:avain></luk:Kutsuja><luk:Hakuehdot><luk:kansallinenOppijanumero>d09afd87a8c6d76b76bbd</luk:kansallinenOppijanumero><luk:organisaatio>00001</luk:organisaatio></luk:Hakuehdot></luk:LukukausiIlmoittautumisetRequest></x:Body></x:Envelope>";
    private static final String serviceName = "http://test.x-road.virta.csc.fi/producer";
    private static final String virtaServiceName = "http://tietovaranto.csc.fi/luku";

    public static HttpResponse getVirtaWS(String XRoadRequest) {
        String manipulatedXRoadRequest = virtaRequestManipulator(XRoadRequest);

        HttpClient client = HttpClientBuilder.create().build();

        //HTTP post
        HttpPost post = new HttpPost("http://virtawstesti.csc.fi:80/luku106/OpiskelijanTiedot");
        post.setHeader("Content-type","text/xml");
        //SOAP message to HTTP body
        HttpEntity entity = new ByteArrayEntity(manipulatedXRoadRequest.getBytes());
        post.setEntity(entity);

        try {
            return client.execute(post);
        }
        catch(IOException e) {
            log.error(e.toString());
        }
        return null;
    }

    // Removes XRoad headers from XRoadRequest
    private static String virtaRequestManipulator(String XRoadRequest){
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(XRoadRequest.getBytes())));

            Element root = doc.getDocumentElement();

            root.removeAttribute("xmlns:xro");
            String att = root.getAttribute(serviceName);


            //Change tns schema
            NamedNodeMap attributes = root.getAttributes();
            for(int i = 0; i < attributes.getLength(); ++i) {
                Node attribute = attributes.item(i);
                String value = attribute.getNodeValue();
                if(attribute.getNodeValue().contains(serviceName)){
                    attribute.setNodeValue(virtaServiceName);
                }
            }


            NodeList children = root.getChildNodes();
            for(int i = 0; i < children.getLength(); ++i){
                //There should be two children; header and body
                Node child = children.item(i);

                //Header should have multiple children, body should have only one
                if(child != null && child.getNodeName().toLowerCase().contains("header")){
                    //Replace XRoad headers with empty SOAP header element with same name
                    root.replaceChild(doc.createElement(child.getNodeName()),child);
                }

                //Add Request appendix to operation input name
                //eg. Opintosuoritukset -> OpintosuorituksetRequest
                if(child != null && child.getNodeName().toLowerCase().contains("body")){
                    Node soapOperationNode = child.getFirstChild().getNextSibling();
                    if(soapOperationNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element elem = (Element) soapOperationNode;
                        String tagName = elem.getTagName();
                        String tagNameForVirta = tagName+"Request";
                        String nsUri = elem.getNamespaceURI();
                        doc.renameNode(soapOperationNode, virtaServiceName, tagNameForVirta);
                        elem.toString();
                    }
                }
            }


            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(domSource, result);
            XRoadRequest = writer.toString();
        }
        catch (IOException e){
            log.error(e.toString());
        }
        catch (SAXException e){
            log.error(e.toString());
        }
        catch (ParserConfigurationException e){
            log.error(e.toString());
        }
        catch(TransformerException e){
            log.error(e.toString());
        }

        //Remove xml definition element, if any
        String sub = StringUtils.substringAfter(XRoadRequest, "?>");
        if(sub != null && sub != ""){
            return sub;
        }
        return XRoadRequest;
    }
}
