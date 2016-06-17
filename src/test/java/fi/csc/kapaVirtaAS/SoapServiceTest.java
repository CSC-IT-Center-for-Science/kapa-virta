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
                .andExpect(content().contentType("text/xml;charset=UTF-8"));
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
        File file = new File(filename);
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            char[] chars = new char[(int) file.length()];
            reader.read(chars);
            content = new String(chars);
            reader.close();
        }
        finally {
            if(reader != null){
                reader.close();
            }
        }
        return content;
    }
}
