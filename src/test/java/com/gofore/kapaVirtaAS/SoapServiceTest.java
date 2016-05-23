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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

    private String testReq1;

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();

        this.testReq1 = readFile("src/test/resources/testRequest1.xml");
    }

    @Test
    public void testResponseSoapHeaders() throws Exception {
        mockMvc.perform(post("/ws/").content(testReq1))
            .andExpect(status().isOk())
                .andExpect(xpath("//*[local-name()='client']").exists())
                .andExpect(xpath("//*[local-name()='xRoadInstance']").exists())
                .andExpect(xpath("//*[local-name()='memberClass']").exists())
                .andExpect(xpath("//*[local-name()='memberCode']").exists())
                .andExpect(xpath("//*[local-name()='subsystemCode']").exists())
                .andExpect(xpath("//*[local-name()='service']").exists())
                .andExpect(xpath("//*[local-name()='serviceCode']").exists())
                .andExpect(xpath("//*[local-name()='userId']").exists())
                .andExpect(xpath("//*[local-name()='id']").exists())
                .andExpect(xpath("//*[local-name()='issue']").exists())
                .andExpect(xpath("//*[local-name()='protocolVersion']").exists());
    }

    @Test
    public void testResponseSoapBody() throws Exception {
        mockMvc.perform(post("/ws/").content(testReq1))
                .andExpect(status().isOk())
                .andExpect(xpath("//*[local-name()='Opintosuoritukset']").exists())
                .andExpect(xpath("//*[local-name()='OpintosuorituksetResponse']").exists());
    }

    @Test
    public void testResponseContentType() throws  Exception {
        mockMvc.perform(post("/ws/").content(testReq1))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/xml"));
    }

    @Test
    public void testResponseClientServiceInformation() throws  Exception {
        mockMvc.perform(post("/ws/").content(testReq1))
                .andExpect(status().isOk())
                .andExpect(xpath("//*[local-name()='service']/*[local-name()='xRoadInstance']/text()").string("FI"))
                .andExpect(xpath("//*[local-name()='service']/*[local-name()='memberClass']/text()").string("COV"))
                .andExpect(xpath("//*[local-name()='service']/*[local-name()='memberCode']/text()").string("1234"))
                .andExpect(xpath("//*[local-name()='service']/*[local-name()='serviceCode']/text()").string("opintosuoritukset"))
                .andExpect(xpath("//*[local-name()='service']/*[local-name()='serviceVersion']/text()").string("v1"));
    }

    private String readFile(String filename) throws Exception {
        String content = null;
        File file = new File(filename); //for ex foo.txt
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            char[] chars = new char[(int) file.length()];
            reader.read(chars);
            content = new String(chars);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(reader !=null){reader.close();}
        }
        return content;
    }
}
