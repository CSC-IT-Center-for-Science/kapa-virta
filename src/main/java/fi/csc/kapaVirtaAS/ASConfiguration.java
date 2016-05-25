package fi.csc.kapaVirtaAS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private static final Logger log = LoggerFactory.getLogger(ASConfiguration.class);
    private static final String confFileName = "adapterServiceConf.xml";

    private final List<String> xroadHeaders;
    private final String xroadSchema;
    private final String xroadIdSchema;
    private final String xroadSchemaPrefixForWSDL;
    private final String xroadIdSchemaPrefixForWSDL;
    private final String adapterServiceSchema;
    private final String adapterServiceSOAPURL;
    private final String adapterServiceWSDLPath;
    private final String virtaWSDLURL;
    private final String virtaVersionForXRoad;
    private final String virtaServiceSchema;

    //Can be used like
    //java -jar kapavirta_as.jar --configuration.path=/path/to/config-file/
    @Value("${configuration.path}")
    private static String confFilePath;

    private File locateInputFile() throws Exception{
        File confFile = new File(confFilePath+confFileName);

        if (confFile.canRead() == true){
            log.info("Config file found from path: "+confFilePath);
            return confFile;
        }

        confFile = new File("../" + confFileName);
        if (confFile.canRead() == true){
            log.info("Config file found from path: ../");
            return confFile;
        }

        confFile = new File("./" + confFileName);
        if (confFile.canRead() == true){
            log.info("Config file found from path: ./");
            return confFile;
        }

        confFile = new File("./static/" + confFileName);
        if (confFile.canRead() == true){
            log.info("Config file found from path: ./static/");
            return confFile;
        }

        confFile = new File("./webapps/" + confFileName);
        if (confFile.canRead() == true) {
            log.info("Config file found from path: ./webapps/");
            return confFile;
        }

        throw new Exception("Could not find config file.");
    }

    public ASConfiguration() {
        Element root = null;

        try {
            File inputFile = locateInputFile();

            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            root = dBuilder.parse(inputFile).getDocumentElement();


        }
        catch(Exception e){
            log.error("Error with configuration file.");
            log.error(e.toString());
        }


        this.xroadHeaders = new ArrayList();
        for (int i = 0; i < root.getElementsByTagName("header").getLength(); ++i) {
            this.xroadHeaders.add(root.getElementsByTagName("header").item(i).getFirstChild().getNodeValue());
        }
        this.xroadSchema = root.getElementsByTagName("xroadSchema").item(0).getFirstChild().getNodeValue();
        this.xroadIdSchema = root.getElementsByTagName("xroadIdSchema").item(0).getFirstChild().getNodeValue();
        this.xroadSchemaPrefixForWSDL = root.getElementsByTagName("xroadSchemaPrefixForWSDL").item(0).getFirstChild().getNodeValue();
        this.xroadIdSchemaPrefixForWSDL = root.getElementsByTagName("xroadIdSchemaPrefixForWSDL").item(0).getFirstChild().getNodeValue();
        this.adapterServiceSchema = root.getElementsByTagName("adapterServiceSchema").item(0).getFirstChild().getNodeValue();
        this.adapterServiceSOAPURL = root.getElementsByTagName("adapterServiceSOAPURL").item(0).getFirstChild().getNodeValue();
        this.adapterServiceWSDLPath = root.getElementsByTagName("adapterServiceWSDLPath").item(0).getFirstChild().getNodeValue();
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

    public String getAdapterServiceWSDLPath() {
        return adapterServiceWSDLPath;
    }

    public String getVirtaWSDLURL() {
        return virtaWSDLURL;
    }

    public String getVirtaVersionForXRoad() { return virtaVersionForXRoad; }

    public String getVirtaServiceSchema() { return virtaServiceSchema; }
}
