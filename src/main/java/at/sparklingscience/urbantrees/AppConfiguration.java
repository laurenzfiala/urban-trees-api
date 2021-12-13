package at.sparklingscience.urbantrees;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.sparklingscience.urbantrees.cms.UserContentConfiguration;
import at.sparklingscience.urbantrees.cms.validation.UserContentValidator;

@Configuration
public class AppConfiguration {
	
	@Value("${at.sparklingscience.urbantrees.dateFormatPattern}")
	private String dateFormat;
	
	@Bean
	protected Jackson2ObjectMapperBuilder jsonObjectMapperBuilder() {
		return Jackson2ObjectMapperBuilder
				.json()
				.timeZone(TimeZone.getTimeZone("UTC"))
				.dateFormat(new SimpleDateFormat(dateFormat));
	}

	@Primary
	@Bean
	public ObjectMapper jsonObjectMapper() {
		return this.jsonObjectMapperBuilder()
				.build();
	}
	
	@Bean("jsonCmsObjectMapper")
	public ObjectMapper jsonCmsObjectMapper() {
		return this.jsonObjectMapperBuilder()
				.defaultViewInclusion(true)
				.visibility(PropertyAccessor.ALL, Visibility.NONE)
				.build();
	}
	
	@Bean
	public PathMatcher userContentPathMatcher() {
		return new AntPathMatcher(UserContentConfiguration.CONTENT_PATH_SEPARATOR);
	}
	
	@Bean
	public UserContentValidator userContentValidator() {
		return new UserContentValidator(this.userContentPathMatcher());
	}
	
	@Bean
	public UserContentConfiguration userContentConfiguration() {
		return new UserContentConfiguration(this.userContentValidator());
	}
	
}
