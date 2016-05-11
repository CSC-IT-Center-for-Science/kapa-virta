package kapaVirtaAS.VirtaProvider;

import com.sun.xml.internal.messaging.saaj.soap.MessageImpl;
import kapaVirtaAS.VirtaClient.VirtaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * Created by joni on 9.5.2016.
 */
@RestController
public class VirtaXRoadEndpoint {
    @Autowired
    private VirtaClient virtaclient;

    @RequestMapping(value="/ws", method= RequestMethod.POST)
    public Object getVirtaResponse(@RequestBody String xroadReq){
        return virtaclient.getVirtaWS(xroadReq);
    }
/*

    private opiskelijatiedot.wsdl.HakuEhdotOrganisaatioVapaa mapHakuehdotOrganisaatioVapaa(HakuEhdotOrganisaatioVapaa hakuehdot) {
        opiskelijatiedot.wsdl.HakuEhdotOrganisaatioVapaa mappedHakuehdot = new opiskelijatiedot.wsdl.HakuEhdotOrganisaatioVapaa();
        mappedHakuehdot.setKansallinenOppijanumero(hakuehdot.getKansallinenOppijanumero());
        mappedHakuehdot.setHenkilotunnus(hakuehdot.getHenkilotunnus());
        return mappedHakuehdot;
    }

    private opiskelijatiedot.wsdl.OpiskelijanTiedotRequest.Hakuehdot mapOpiskelijanTiedotHakuehdot(OpiskelijanTiedot_Type.Hakuehdot hakuehdot) {
        opiskelijatiedot.wsdl.OpiskelijanTiedotRequest.Hakuehdot mappedHakuehdot = new opiskelijatiedot.wsdl.OpiskelijanTiedotRequest.Hakuehdot();
        mappedHakuehdot.setKansallinenOppijanumero(hakuehdot.getKansallinenOppijanumero());
        mappedHakuehdot.setHenkilotunnus(hakuehdot.getHenkilotunnus());
        return mappedHakuehdot;
    }

    private opiskelijatiedot.wsdl.Kutsuja mapKutsuja(Kutsuja kutsuja){
        opiskelijatiedot.wsdl.Kutsuja mappedKutsuja = new opiskelijatiedot.wsdl.Kutsuja();
        mappedKutsuja.setJarjestelma(kutsuja.getJarjestelma());
        mappedKutsuja.setAvain(kutsuja.getAvain());
        mappedKutsuja.setTunnus(kutsuja.getTunnus());
        return mappedKutsuja;
    }


    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "Tutkinnot")
    @ResponsePayload
    public opiskelijatiedot.wsdl.TutkinnotResponse getTutkinnotResponse(@RequestPayload Tutkinnot xroadRequest){

        opiskelijatiedot.wsdl.TutkinnotRequest virtaReq = new opiskelijatiedot.wsdl.TutkinnotRequest();

        virtaReq.setHakuehdot(mapHakuehdotOrganisaatioVapaa(xroadRequest.getHakuehdot()));
        virtaReq.setKutsuja(mapKutsuja(xroadRequest.getKutsuja()));

        return virtaclient.getTutkinnot(virtaReq);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "Opiskeluoikeudet")
    @ResponsePayload
    public opiskelijatiedot.wsdl.OpiskeluoikeudetResponse getOpiskeluoikeudetResponse(@RequestPayload Opiskeluoikeudet xroadRequest){
        opiskelijatiedot.wsdl.OpiskeluoikeudetRequest virtaReq = new opiskelijatiedot.wsdl.OpiskeluoikeudetRequest();

        virtaReq.setHakuehdot(mapHakuehdotOrganisaatioVapaa(xroadRequest.getHakuehdot()));
        virtaReq.setKutsuja(mapKutsuja(xroadRequest.getKutsuja()));

        return virtaclient.getVirtaWS((Object)virtaReq);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "OpiskelijanTiedot")
    @ResponsePayload
    public Object getOpiskelijanTiedot(@RequestPayload OpiskelijanTiedot_Type xroadRequest){
        opiskelijatiedot.wsdl.OpiskelijanTiedotRequest virtaReq = new opiskelijatiedot.wsdl.OpiskelijanTiedotRequest();

        virtaReq.setHakuehdot(mapOpiskelijanTiedotHakuehdot(xroadRequest.getHakuehdot()));
        virtaReq.setKutsuja(mapKutsuja(xroadRequest.getKutsuja()));

        return virtaclient.getVirtaWS((Object)virtaReq);
    }

    //@PayloadRoot(namespace = NAMESPACE_URI, localPart = "OpiskelijanKaikkiTiedot")
    @PayloadRoot(namespace = NAMESPACE_URI);
    @ResponsePayload
    public opiskelijatiedot.wsdl.OpiskelijanKaikkiTiedotResponse getOpiskelijanKaikkiTiedot(@RequestPayload OpiskelijanKaikkiTiedot xroadRequest){
        opiskelijatiedot.wsdl.OpiskelijanKaikkiTiedotRequest virtaReq = new opiskelijatiedot.wsdl.OpiskelijanKaikkiTiedotRequest();

        virtaReq.setHakuehdot(mapHakuehdotOrganisaatioVapaa(xroadRequest.getHakuehdot()));
        virtaReq.setKutsuja(mapKutsuja(xroadRequest.getKutsuja()));

        return virtaclient.getVirtaWS((Object)virtaReq);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "Opintosuoritukset")
    @ResponsePayload
    public opiskelijatiedot.wsdl.OpintosuorituksetResponse getOpiskelijanKaikkiTiedot(@RequestPayload Opintosuoritukset xroadRequest){
        opiskelijatiedot.wsdl.OpintosuorituksetRequest virtaReq = new opiskelijatiedot.wsdl.OpintosuorituksetRequest();

        virtaReq.setHakuehdot(mapHakuehdotOrganisaatioVapaa(xroadRequest.getHakuehdot()));
        virtaReq.setKutsuja(mapKutsuja(xroadRequest.getKutsuja()));

        return virtaclient.getOpintosuoritukset(virtaReq);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "LukukausiIlmoittautumiset")
    @ResponsePayload
    public opiskelijatiedot.wsdl.LukukausiIlmoittautumisetResponse getLukukausiIlmoittautumiset(@RequestPayload LukukausiIlmoittautumiset xroadRequest){
        opiskelijatiedot.wsdl.LukukausiIlmoittautumisetRequest virtaReq = new opiskelijatiedot.wsdl.LukukausiIlmoittautumisetRequest();

        virtaReq.setHakuehdot(mapHakuehdotOrganisaatioVapaa(xroadRequest.getHakuehdot()));
        virtaReq.setKutsuja(mapKutsuja(xroadRequest.getKutsuja()));

        return virtaclient.getLukukausiIlmoittautumiset(virtaReq);
    }
    */
}
