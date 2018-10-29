package at.sparklingscience.urbantrees;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Spring MVC web configuration.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/26
 */
@Configuration
public class WebConfiguration extends WebMvcConfigurerAdapter {

  /**
   * Ignore "Accepts"-header and set json as default.
   * Also ignore file endings.
   */
  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    configurer.favorPathExtension(false)
            .ignoreAcceptHeader(true)
            .useJaf(false)
            .defaultContentType(MediaType.APPLICATION_JSON)
            .mediaType("json", MediaType.APPLICATION_JSON);
  }
  
  @Override
  public void addCorsMappings(CorsRegistry registry) {
      registry.addMapping("/**")
              .allowedMethods("GET", "POST", "PUT");
  }
  
}