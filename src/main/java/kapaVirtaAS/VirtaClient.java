package kapaVirtaAS;

/**
 * Created by joni on 9.5.2016.
 */
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class VirtaClient {

    private static final Logger log = LoggerFactory.getLogger(VirtaClient.class);
    private final String testSoapMessage = "<x:Envelope xmlns:x=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:luk=\"http://tietovaranto.csc.fi/luku\"><x:Header/><x:Body><luk:LukukausiIlmoittautumisetRequest><luk:Kutsuja><luk:jarjestelma/><luk:tunnus/><luk:avain>salaisuus</luk:avain></luk:Kutsuja><luk:Hakuehdot><luk:kansallinenOppijanumero>d09afd87a8c6d76b76bbd</luk:kansallinenOppijanumero><luk:organisaatio>00001</luk:organisaatio></luk:Hakuehdot></luk:LukukausiIlmoittautumisetRequest></x:Body></x:Envelope>";

    public VirtaClient() {
    }

    public HttpResponse getVirtaWS(String virtaRequestMessage) {
        if(!validateVirtaRequest(virtaRequestMessage)){
            log.error("Virta request validation failed");
            return null;
        }

        try {
            HttpClient client = HttpClientBuilder.create().build();

            //HTTP post
            HttpPost post = new HttpPost("http://virtawstesti.csc.fi:80/luku106/OpiskelijanTiedot");
            post.setHeader("Content-type","text/xml");
            //SOAP message to HTTP body
            HttpEntity entity = new ByteArrayEntity(virtaRequestMessage.getBytes());
            post.setEntity(entity);
            return client.execute(post);
        }
        catch(IOException e) {
            log.error(e.getStackTrace().toString());
        }

        return null;
    }

    private static boolean validateVirtaRequest(String virtaRequest){
        return true;
        /*
        try {
            // parse an SOAP-request into a DOM tree
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = parser.parse(new InputSource(new ByteArrayInputStream(virtaRequest.getBytes())));

            // create a SchemaFactory capable of understanding WXS schemas
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // load a WXS schema, represented by a Schema instance
            Source schemaFile = new StreamSource(new File("target/wsdl/opiskelijatiedot.wsdl"));
            Schema schema = factory.newSchema(schemaFile);

            // create a Validator instance, which can be used to validate an instance document
            Validator validator = schema.newValidator();

            // validate the DOM tree
            validator.validate(new DOMSource(document));
        }
        catch (SAXException e){
            log.error(e.toString());
            return false;
        }
        catch (IOException e){
            log.error(e.toString());
            return false;
        }
        catch (ParserConfigurationException e){
            log.error(e.toString());
            return false;
        }

        return true;
        */
    }

    /*
    // Removes XRoad headers from XRoadRequest
    private String transformXRoadRequestToVirta(String XRoadRequest){
        //Remove xml definition element, if any
        String sub = StringUtils.substringAfter(XRoadRequest, "?>");
        if(sub != null && sub != ""){
            XRoadRequest = sub;
        }

        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(XRoadRequest.getBytes())));
            Element root = doc.getDocumentElement();

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
                //There should be two children under the root node: header and body
                Node child = children.item(i);

                //Replace XRoad headers with empty SOAP header element with same name
                if(child != null && child.getNodeName().toLowerCase().contains("header")){
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

        return XRoadRequest;
    }
    */
}
