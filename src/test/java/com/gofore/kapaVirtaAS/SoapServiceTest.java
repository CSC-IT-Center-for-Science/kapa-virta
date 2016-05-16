package com.gofore.kapaVirtaAS;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;


import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Created by joni on 16.5.2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebAppConfiguration
public class SoapServiceTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private final String testRequest1 =
            "<x:Envelope xmlns:x=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                "xmlns:pro=\"http://test.x-road.virta.csc.fi/producer\" " +
                    "xmlns:xro=\"http://x-road.eu/xsd/xroad.xsd\">\n" +
            "    <x:Header>\n" +
            "        <xro:client/>\n" +
            "        <xro:service/>\n" +
            "        <xro:userId>ok</xro:userId>\n" +
            "        <xro:id>ok</xro:id>\n" +
            "        <xro:issue>ok</xro:issue>\n" +
            "        <xro:protocolVersion>6</xro:protocolVersion>\n" +
            "    </x:Header>\n" +
            "    <x:Body>\n" +
            "        <pro:Tutkinnot>\n" +
            "            <pro:Kutsuja>\n" +
            "                <pro:jarjestelma/>\n" +
            "                <pro:tunnus/>\n" +
            "                <pro:avain>salaisuus</pro:avain>\n" +
            "            </pro:Kutsuja>\n" +
            "            <pro:Hakuehdot>\n" +
            "                <pro:kansallinenOppijanumero>aed09afd87a8c6d76b76bbd</pro:kansallinenOppijanumero>\n" +
            "                <pro:organisaatio>00001</pro:organisaatio>\n" +
            "            </pro:Hakuehdot>\n" +
            "        </pro:Tutkinnot>\n" +
            "    </x:Body>\n" +
            "</x:Envelope>";
    private final String testResponseHeaders1 =
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "   <SOAP-ENV:Header xmlns:xro=\"http://x-road.eu/xsd/xroad.xsd\">\n" +
            "        <xro:client/>\n" +
            "        <xro:service/>\n" +
            "        <xro:userId>ok</xro:userId>\n" +
            "        <xro:id>ok</xro:id>\n" +
            "        <xro:issue>ok</xro:issue>\n" +
            "        <xro:protocolVersion>6</xro:protocolVersion>\n" +
            "   </SOAP-ENV:Header>" +
            "   <SOAP-ENV:Body>" +
            "       <virtaluku:TutkinnotResponse xmlns:virtaluku=\"http://test.x-road.virta.csc.fi/producer\">" +
            "           <virta:Opintosuoritukset xmlns:virta=\"urn:mace:funet.fi:virta/2015/09/01\"/>" +
            "       </virtaluku:TutkinnotResponse>" +
            "       <pro:Tutkinnot/>" +
            "   </SOAP-ENV:Body>" +
            "</SOAP-ENV:Envelope>";

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testResponseXRoadHeaders() throws Exception {
        mockMvc.perform(post("/ws/").content(testRequest1))
            .andExpect(status().isOk())
                .andExpect(xpath("//*[local-name()='client']").exists())
                .andExpect(xpath("//*[local-name()='service']").exists())
                .andExpect(xpath("//*[local-name()='userId']").exists())
                .andExpect(xpath("//*[local-name()='id']").exists())
                .andExpect(xpath("//*[local-name()='issue']").exists())
                .andExpect(xpath("//*[local-name()='protocolVersion']").exists());
    }
}
