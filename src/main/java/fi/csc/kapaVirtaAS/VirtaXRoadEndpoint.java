package fi.csc.kapaVirtaAS;

import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by joni on 9.5.2016.
 */
@RestController
public class VirtaXRoadEndpoint {
    private static final Logger log = LoggerFactory.getLogger(VirtaXRoadEndpoint.class);
    private final String[] headersToCopy = {"Content-Type", "Connection", "SOAPAction"};
    private final String ERROR_MESSAGE = "XRoad-Virta adapterservice encountered internal server error";

    @RequestMapping(value="/", method= RequestMethod.GET)
    public String healthCheck(){
        return "KapaVirta adapter service running.";
    }

    @RequestMapping(value="/ws", method= RequestMethod.POST)
    public ResponseEntity<String> getVirtaResponse(@RequestBody String XRoadRequestMessage) throws Exception{
        SOAPMessageTransformer messageTransformer = new SOAPMessageTransformer(new ASConfiguration());
        VirtaClient virtaClient = new VirtaClient();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);

        //Transform SOAP-request to Virta
        String virtaRequestMessage;
        try {
            virtaRequestMessage = messageTransformer.transform(XRoadRequestMessage,
                    SOAPMessageTransformer.MessageDirection.XRoadToVirta);
        }
        catch (Exception e){
            log.error(e.toString());
            return new ResponseEntity<>(ERROR_MESSAGE, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //Send transformed SOAP-request to Virta
        HttpResponse virtaResponse = virtaClient.getVirtaWS(virtaRequestMessage);
        if(virtaResponse == null){
            new ResponseEntity<>(ERROR_MESSAGE, headers, HttpStatus.INTERNAL_SERVER_ERROR);
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
        } catch(Exception e){
            log.error(e.getMessage());
            return new ResponseEntity<>(ERROR_MESSAGE, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //Transform SOAP-response to XRoad
        String XRoadResponseMessage;
        try {
            XRoadResponseMessage = messageTransformer.transform(virtaResponseMessage,
                    SOAPMessageTransformer.MessageDirection.VirtaToXRoad);
        }catch (Exception e){
            log.error(e.toString());
            return new ResponseEntity<>(ERROR_MESSAGE, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //Validate transformed SOAP-response
        if(!validateXRoadResponse(XRoadResponseMessage)){
            log.error("Validate failed");
            return new ResponseEntity<>(ERROR_MESSAGE, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }


        return new ResponseEntity<>(XRoadResponseMessage, headers, HttpStatus.OK);
    }

    private static boolean validateXRoadResponse(String XRoadResponse){

        return true;
    }
}
