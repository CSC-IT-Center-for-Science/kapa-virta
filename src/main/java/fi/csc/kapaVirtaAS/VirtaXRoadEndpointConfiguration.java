package fi.csc.kapaVirtaAS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by joni on 24.5.2016.
 */

@Configuration
@EnableWebMvc
public class VirtaXRoadEndpointConfiguration extends WebMvcConfigurerAdapter {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            ASConfiguration conf = new ASConfiguration();
            String resourceLocation = conf.getAdapterServiceWSDLPath().replace(".","").replace("..","");

            log.info("Path to kapavirta_as.wsdl file: "+resourceLocation);
            registry
                    .addResourceHandler("/resources/**")
                    .addResourceLocations("file:"+resourceLocation);
        }
        catch(Exception e) {
            log.error("Error in WSDL resource initialization.");
            log.error(e.toString());
        }
    }
}
