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
