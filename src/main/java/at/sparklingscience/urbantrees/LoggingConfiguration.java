package at.sparklingscience.urbantrees;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Logging configuartion.
 * Configures logger access for the whole application.
 * 
 * @author Laurenz Fiala
 * @since 2020/04/19
 */
@Configuration
public class LoggingConfiguration {
	
	@Bean
	@Scope("prototype")
	public Logger logger(InjectionPoint injectionPoint){

	    return LoggerFactory.getLogger(injectionPoint.getMethodParameter().getContainingClass());

	}
    
}
