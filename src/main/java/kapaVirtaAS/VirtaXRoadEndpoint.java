package kapaVirtaAS;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Created by joni on 9.5.2016.
 */
@RestController
public class VirtaXRoadEndpoint {
    private static final Logger log = LoggerFactory.getLogger(VirtaClient.class);
    private final String[] headersToCopy = {"Content-Type", "Connection", "SOAPAction"};
    private final String ERROR_MESSAGE = "XRoad-Virta adapterservice encountered internal server error";

    @RequestMapping(value="/ws", method= RequestMethod.POST)
    public String getVirtaResponse(@RequestBody String XRoadRequestMessage, HttpServletResponse response) throws Exception{
        SOAPMessageTransformer messageTransformer = new SOAPMessageTransformer();
        VirtaClient virtaClient = new VirtaClient();

        //Transform SOAP-request to Virta
        String virtaRequestMessage;
        try {
            virtaRequestMessage = messageTransformer.transform(XRoadRequestMessage,
                    SOAPMessageTransformer.MessageDirection.XRoadToVirta);
        }
        catch (Exception e){
            log.error(e.toString());
            return ERROR_MESSAGE;
         }

        //Send transformed SOAP-request to Virta
        HttpResponse virtaResponse = virtaClient.getVirtaWS(virtaRequestMessage);
        if(virtaResponse == null){
            response.setStatus(500);
            return ERROR_MESSAGE;
        }

        //Exctract response message
        String virtaResponseMessage;
        try {
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(virtaResponse.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            virtaResponseMessage = result.toString();
        }
        catch(IOException e){
            log.error(e.getMessage());
            return ERROR_MESSAGE;
        }

        //Transform SOAP-response to XRoad
        String XRoadResponseMessage;
        try {
            XRoadResponseMessage = messageTransformer.transform(virtaResponseMessage ,
                    SOAPMessageTransformer.MessageDirection.VirtaToXRoad);
        }
        catch (Exception e){
            log.error(e.toString());
            return ERROR_MESSAGE;
        }

        //Validate transformed SOAP-response
        if(!validateXRoadResponse(XRoadResponseMessage)){
            log.error("Validate failed");
            response.setStatus(500);
            return "XRoad-Virta adaterservice encountered internal server error";
        }

        // Set HTTP-headers to SOAP-response
        for(Header virtaHeader : virtaResponse.getAllHeaders()){
            if(Arrays.asList(headersToCopy).contains(virtaHeader.getName())){
                response.setHeader(virtaHeader.getName(), virtaHeader.getValue());
            }
        }

        return XRoadResponseMessage;
    }

    private static boolean validateXRoadResponse(String XRoadResponse){

        return true;
    }

    /*
    private String transformVirtaResponseToXRoad(String virtaResponse){
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(virtaResponse.getBytes())));
            Element root = doc.getDocumentElement();

            //Change tns schema
            NamedNodeMap attributes = root.getAttributes();
            for(int i = 0; i < attributes.getLength(); ++i) {
                Node attribute = attributes.item(i);
                String value = attribute.getNodeValue();
                if(attribute.getNodeValue().contains(virtaServiceName)){
                    attribute.setNodeValue(serviceName);
                }
            }

            NodeList children = root.getChildNodes();
            for(int i = 0; i < children.getLength(); ++i){
                //There should be two children under the root node: header and body
                Node child = children.item(i);

                //Replace empty SOAP header element with XRoad headers
                if(child != null && child.getNodeName().toLowerCase().contains("header")){
                    root.replaceChild(doc.createElement(child.getNodeName()),child);
                }

                //Remove Request appendix to operation input name
                //eg. OpintosuorituksetRequest -> Opintosuoritukset
                if(child != null && child.getNodeName().toLowerCase().contains("body")){
                    Node soapOperationNode = child.getFirstChild().getNextSibling();
                    if(soapOperationNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element elem = (Element) soapOperationNode;
                        String tagNameForXRoad =  StringUtils.substringBefore(elem.getTagName(),"Request");
                        doc.renameNode(soapOperationNode, serviceName, tagNameForXRoad);
                        elem.toString();
                    }
                }
            }

            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(domSource, result);
            virtaResponse = writer.toString();

            return virtaResponse;
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
    return null;
    }
    */
}
