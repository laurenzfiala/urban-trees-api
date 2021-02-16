package at.sparklingscience.urbantrees;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class AppConfiguration {
	
	@Value("${at.sparklingscience.urbantrees.dateFormatPattern}")
	private String dateFormat;

	// TODO check if this mapper is right for CMS (de)serialization
	@Bean
	public ObjectMapper jsonObjectMapper() {
		return Jackson2ObjectMapperBuilder
				.json()
				.timeZone(TimeZone.getTimeZone("UTC"))
				.dateFormat(new SimpleDateFormat(dateFormat))
				.defaultViewInclusion(true)
				.build();
	}
	
}
