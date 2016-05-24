package fi.csc.kapaVirtaAS;

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
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            ASConfiguration conf = new ASConfiguration();
            registry
                    .addResourceHandler("/resources/**")
                    .addResourceLocations("/"+conf.getAdapterServiceWSDLPath());
        }
        catch(Exception e) {

        }
    }
}
