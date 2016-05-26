package fi.csc.kapaVirtaAS;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/**
 * Created by joni on 26.5.2016.
 */
public class FaultMessageService {

    private final String reqValidFail = "Virta adapter service failed to process request message";
    private final String resValidFail = "Virta adapter service failed to process response message";

    public FaultMessageService() {
    }

    public String getReqValidFail() {
        return reqValidFail;
    }

    public String getResValidFail() {
        return resValidFail;
    }

    public String generateSOAPFault(String message, String faultString, Node xroadHeaderElement) throws Exception {
        DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(getClass().getResourceAsStream("/FaultMessageBody.xml")));
        doc.setXmlVersion("1.0");
        doc.normalizeDocument();
        Element root = doc.getDocumentElement();

        for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
            if(child.getNodeName().toLowerCase().contains("header")){
                //Append XRoad SOAP-headers back
                if(xroadHeaderElement != null && xroadHeaderElement.getChildNodes() != null) {
                    for (int j = 0; j < xroadHeaderElement.getChildNodes().getLength(); ++j) {
                        child.appendChild(doc.importNode(xroadHeaderElement.getChildNodes().item(j), true));
                    }
                }
            }
        }

        root.getElementsByTagName("detail").item(0).setTextContent(message);
        root.getElementsByTagName("faultstring").item(0).setTextContent(faultString);

        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();
    }
}