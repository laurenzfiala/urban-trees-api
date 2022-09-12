package at.sparklingscience.urbantrees;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC web configuration.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/26
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {
	
	@Value("${at.sparklingscience.urbantrees.corsAllowedOriginPatterns}")
	private String corsAllowedOriginPatterns;
		
	/**
	 * Set json as default and disallow unregistered extensions.
	 */
	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
	  configurer
	      .useRegisteredExtensionsOnly(true)
	      .defaultContentType(MediaType.APPLICATION_JSON)
	      .mediaType("json", MediaType.APPLICATION_JSON);
	}
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
	    registry.addMapping("/**")
	            .allowedMethods("GET", "POST", "PUT", "DELETE")
	            .allowedOriginPatterns(this.corsAllowedOriginPatterns)
	            .allowCredentials(true);
	}
	
	/**
	 * Date & time formatter for web header fields.
	 * {@link DateTimeFormatter#RFC_1123_DATE_TIME} is no candidate,
	 * see https://stackoverflow.com/a/26367834/2740014
	 */
	@Bean
	public DateTimeFormatter httpHeaderDateFormatter() {
		return DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
							    .withZone(ZoneId.of("GMT"));
	}
  
}