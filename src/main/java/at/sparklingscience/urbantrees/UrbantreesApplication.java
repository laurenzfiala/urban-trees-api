package at.sparklingscience.urbantrees;

import java.nio.charset.Charset;
import java.util.TimeZone;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("at.sparklingscience.urbantrees.mapper")
public class UrbantreesApplication {
	
	/**
	 * Charset to be used for all file and other string operations.
	 */
	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
	
	public static void main(String[] args) {
		
		// Manually set the applications' timestamp to UTC for time conversion to work.
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		
		SpringApplication.run(UrbantreesApplication.class, args);
	}
	
}