package kapaVirtaAS.VirtaClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

/**
 * Created by joni on 9.5.2016.
 */

@Configuration
public class VirtaConfiguration {
    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("opiskelijatiedot.wsdl");
        return marshaller;
    }

    @Bean
    public VirtaClient virtaClient(Jaxb2Marshaller marshaller) {
        VirtaClient client = new VirtaClient();
        client.setDefaultUri("http://virtawstesti.csc.fi/luku106/opiskelijatiedot.wsdl");
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        return client;
    }

}
