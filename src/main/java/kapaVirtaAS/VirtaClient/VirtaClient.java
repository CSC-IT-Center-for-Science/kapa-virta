package kapaVirtaAS.VirtaClient;

/**
 * Created by joni on 9.5.2016.
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import opiskelijatiedot.wsdl.*;

public class VirtaClient extends WebServiceGatewaySupport {

    private static final Logger log = LoggerFactory.getLogger(VirtaClient.class);

    public Object getVirtaWS(String xroadRequest){
        return getWebServiceTemplate().marshalSendAndReceive(xroadRequest);
    }

    public <RES> getVirtaResponse(<A> request){
        return (TutkinnotResponse) getWebServiceTemplate().marshalSendAndReceive(request);
    }

    public OpiskeluoikeudetResponse getVirtaResponse(OpiskeluoikeudetRequest request){
        return (OpiskeluoikeudetResponse) getWebServiceTemplate().marshalSendAndReceive(request);
    }

    public OpiskelijanTiedotResponse getVirtaResponse(OpiskelijanTiedotRequest request){
        return (OpiskelijanTiedotResponse) getWebServiceTemplate().marshalSendAndReceive(request);
    }

    public OpiskelijanKaikkiTiedotResponse getVirtaResponse(OpiskelijanKaikkiTiedotRequest request){
        return (OpiskelijanKaikkiTiedotResponse) getWebServiceTemplate().marshalSendAndReceive(request);
    }

    public OpintosuorituksetResponse getVirtaResponse(OpintosuorituksetRequest request){
        return (OpintosuorituksetResponse) getWebServiceTemplate().marshalSendAndReceive(request);
    }

    public LukukausiIlmoittautumisetResponse getVirtaResponse(LukukausiIlmoittautumisetRequest request){
        return (LukukausiIlmoittautumisetResponse) getWebServiceTemplate().marshalSendAndReceive(request);
    }
}
