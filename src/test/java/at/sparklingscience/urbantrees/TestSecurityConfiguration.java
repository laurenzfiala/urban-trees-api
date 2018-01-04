package at.sparklingscience.urbantrees;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Security configuration with higher precedence only for testing.
 * This way, the tests don't need to add authorization.
 * 
 * @author Laurenz Fiala
 * @since 2017/01/04
 */
@Configuration
@Order(99)
public class TestSecurityConfiguration extends WebSecurityConfigurerAdapter {
	
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
        	.anonymous()
        	.and()
        		.csrf().disable();

    }

}
