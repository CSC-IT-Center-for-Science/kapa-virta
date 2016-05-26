package fi.csc.kapaVirtaAS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by joni on 9.5.2016.
 */

@ComponentScan
@SpringBootApplication
public class Application extends SpringBootServletInitializer {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    public static void main(String[] args) throws Exception {
        //Configuration loading or WSDL-generation might throw
        //and in that case adapter service should not start
        ASConfiguration conf = new ASConfiguration();
        WSDLManipulator wsdlManipulator = new WSDLManipulator(conf);
        log.info("Adapter service configuration loaded.");
        wsdlManipulator.generateVirtaKapaWSDL();
        log.info("Adapter service WSDL generated.");


        SpringApplication.run(Application.class, args);
    }
}