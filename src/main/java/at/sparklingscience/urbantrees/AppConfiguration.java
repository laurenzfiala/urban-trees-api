package at.sparklingscience.urbantrees;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.sparklingscience.urbantrees.service.AdminService;

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
				.visibility(PropertyAccessor.ALL, Visibility.ANY)
				.build();
	}
	
	@Bean("jsonCmsObjectMapper")
	public ObjectMapper jsonCmsObjectMapper() {
		return this.jsonObjectMapperBuilder()
				.defaultViewInclusion(true)
				.visibility(PropertyAccessor.ALL, Visibility.NONE)
				.build();
	}

	@Bean("loginQrFont")
	public Font loginQrFont() {
		try {
			return Font.createFont(
						Font.TRUETYPE_FONT,
						AdminService.class.getResourceAsStream("/fonts/Quicksand-SemiBold.ttf")
					).deriveFont(30f);
		} catch (IOException | FontFormatException e) {
			throw new RuntimeException("Failed to load font for login QRs: " + e.getMessage(), e);
		}
	}
	
}
