package at.sparklingscience.urbantrees;

import java.util.TimeZone;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@SpringBootApplication
@MapperScan("at.sparklingscience.urbantrees.mapper")
public class UrbantreesApplication {
	
	public static void main(String[] args) {
		
		// Manually set the applications' timestamp to UTC for time conversion to work.
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		
		SpringApplication.run(UrbantreesApplication.class, args);
	}
	
}