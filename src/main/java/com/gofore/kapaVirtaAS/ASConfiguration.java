package com.gofore.kapaVirtaAS;

import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by joni on 23.5.2016.
 */
public class ASConfiguration {
    private static final String confFileName = "adapterServiceConf.xml";

    private final List<String> xroadHeaders;
    private final String xroadSchema;
    private final String xroadIdSchema;
    private final String xroadSchemaPrefixForWSDL;
    private final String xroadIdSchemaPrefixForWSDL;
    private final String adapterServiceSchema;
    private final String adapterServiceSOAPURL;
    private final String virtaWSDLURL;
    private final String virtaVersionForXRoad;
    private final String virtaServiceSchema;

    public ASConfiguration() throws Exception {
        File inputFile = new File("../"+ confFileName);
        if(inputFile.canRead() == false){
            //For developing environment
            inputFile = new File("target/"+ confFileName);
        }

        DocumentBuilderFactory dbFactory
                = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Element root = dBuilder.parse(inputFile).getDocumentElement();

        this.xroadHeaders = new ArrayList();
        for(int i = 0; i < root.getElementsByTagName("header").getLength(); ++i){
            this.xroadHeaders.add(root.getElementsByTagName("header").item(i).getFirstChild().getNodeValue());
        }

        this.xroadSchema = root.getElementsByTagName("xroadSchema").item(0).getFirstChild().getNodeValue();
        this.xroadIdSchema = root.getElementsByTagName("xroadIdSchema").item(0).getFirstChild().getNodeValue();
        this.xroadSchemaPrefixForWSDL = root.getElementsByTagName("xroadSchemaPrefixForWSDL").item(0).getFirstChild().getNodeValue();
        this.xroadIdSchemaPrefixForWSDL = root.getElementsByTagName("xroadIdSchemaPrefixForWSDL").item(0).getFirstChild().getNodeValue();
        this.adapterServiceSchema = root.getElementsByTagName("adapterServiceSchema").item(0).getFirstChild().getNodeValue();
        this.adapterServiceSOAPURL = root.getElementsByTagName("adapterServiceSOAPURL").item(0).getFirstChild().getNodeValue();
        this.virtaWSDLURL = root.getElementsByTagName("virtaWSDLURL").item(0).getFirstChild().getNodeValue();
        this.virtaVersionForXRoad = root.getElementsByTagName("virtaVersionForXRoad").item(0).getFirstChild().getNodeValue();
        this.virtaServiceSchema = root.getElementsByTagName("virtaServiceSchema").item(0).getFirstChild().getNodeValue();
    }

    public List<String> getXroadHeaders() { return xroadHeaders; }

    public String getXroadSchema() {
        return xroadSchema;
    }

    public String getXroadIdSchema() {
        return xroadIdSchema;
    }

    public String getXroadSchemaPrefixForWSDL() { return xroadSchemaPrefixForWSDL; }

    public String getXroadIdSchemaPrefixForWSDL() { return xroadIdSchemaPrefixForWSDL; }

    public String getAdapterServiceSchema() {
        return adapterServiceSchema;
    }

    public String getAdapterServiceSOAPURL() {
        return adapterServiceSOAPURL;
    }

    public String getVirtaWSDLURL() {
        return virtaWSDLURL;
    }

    public String getVirtaVersionForXRoad() { return virtaVersionForXRoad; }

    public String getVirtaServiceSchema() { return virtaServiceSchema; }
}
