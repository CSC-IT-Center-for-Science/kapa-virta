package kapaVirtaAS.VirtaProvider;

import kapaVirtaAS.VirtaClient.VirtaClient;
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

    @RequestMapping(value="/ws", method= RequestMethod.POST)
    public String getVirtaResponse(@RequestBody String xroadReq, HttpServletResponse response){
        HttpResponse virtaResponse = VirtaClient.getVirtaWS(xroadReq);
        for(Header virtaHeader : virtaResponse.getAllHeaders()){
            if(Arrays.asList(headersToCopy).contains(virtaHeader.getName())){
                response.setHeader(virtaHeader.getName(), virtaHeader.getValue());
            }
        }

        try {
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(virtaResponse.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        }
        catch(IOException e){
            log.error(e.toString());
            return null;
        }

    }
}
