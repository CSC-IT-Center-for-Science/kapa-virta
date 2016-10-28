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

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
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
import org.w3c.dom.DOMException;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@RestController
public class VirtaXRoadEndpoint {
    private static final Logger log = LoggerFactory.getLogger(VirtaXRoadEndpoint.class);
    private static final ASConfiguration conf = new ASConfiguration();
    private final String ERROR_MESSAGE = "XRoad-Virta adapterservice encountered error with status: ";

    @RequestMapping(value="/", method= RequestMethod.GET)
    public String healthCheck(){
        return "KapaVirta adapter service running.";
    }


    @RequestMapping(value="/ws", method= RequestMethod.POST)
    public ResponseEntity<String> getVirtaResponse(@RequestBody String XRoadRequestMessage) throws Exception{
        FaultMessageService faultMessageService = new FaultMessageService();
        MessageTransformer messageTransformer = new MessageTransformer(conf, faultMessageService);
        VirtaClient virtaClient = new VirtaClient(conf);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(new MediaType("text","xml", Charsets.UTF_8));
        HttpResponse virtaResponse;

        try {
            String virtaRequestMessage = messageTransformer.transform(XRoadRequestMessage,
                    MessageTransformer.MessageDirection.XRoadToVirta);
            //Send transformed SOAP-request to Virta
            virtaResponse = virtaClient.getVirtaWS(virtaRequestMessage, messageTransformer.createAuthenticationString(XRoadRequestMessage));
        } catch (Exception e){
            log.error(e.toString());
            HttpStatus errorStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            String errorMessage = ERROR_MESSAGE;
            if(e instanceof DOMException){
                errorStatus = HttpStatus.BAD_REQUEST;
                errorMessage = "Request SOAP-headers did not contain client identifiers (http://x-road.eu/xsd/identifiers)";
            }
            return new ResponseEntity<>(faultMessageService.generateSOAPFault(errorMessage,
                    faultMessageService.getReqValidFail(),
                    messageTransformer.getXroadHeaderElement()),
                    httpHeaders, errorStatus);
        }

        try {
            if(virtaResponse.getStatusLine().getStatusCode() != 200){
                log.error(virtaResponse.getStatusLine().getReasonPhrase());
                throw new HttpResponseException(virtaResponse.getStatusLine().getStatusCode(), virtaResponse.getStatusLine().getReasonPhrase());
            }
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(virtaResponse.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            String virtaResponseMessage = result.toString();

            return new ResponseEntity<>(messageTransformer.transform(virtaResponseMessage,
                    MessageTransformer.MessageDirection.VirtaToXRoad), httpHeaders, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.toString());
            HttpStatus status = HttpStatus.valueOf(virtaResponse.getStatusLine().getStatusCode());
            if(status.value() == 200) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            } else if (IOUtils.toString(virtaResponse.getEntity().getContent()).toLowerCase().contains("access denied")) {
                status = HttpStatus.FORBIDDEN;
            }
            return new ResponseEntity<>(faultMessageService.generateSOAPFault(ERROR_MESSAGE+status.name(),
                    faultMessageService.getResValidFail(),
                    messageTransformer.getXroadHeaderElement()),
                    httpHeaders, status);
        }
    }
}
