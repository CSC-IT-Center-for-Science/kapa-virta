package fi.csc.kapaVirtaAS;

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

    private static final ASConfiguration conf = new ASConfiguration();
    private static final Logger log = LoggerFactory.getLogger(VirtaClient.class);

    public VirtaClient() {
    }

    public HttpResponse getVirtaWS(String virtaRequestMessage) {
        try {
            HttpClient client = HttpClientBuilder.create().build();

            //HTTP post
            HttpPost post = new HttpPost(conf.getVirtaSOAPURL());
            post.setHeader("Content-type","text/xml");
            //SOAP message to HTTP body
            HttpEntity entity = new ByteArrayEntity(virtaRequestMessage.getBytes());
            post.setEntity(entity);
            return client.execute(post);
        }
        catch(IOException e) {
            log.error(e.toString());
        }

        return null;
    }
}
