package at.sparklingscience.urbantrees;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import at.sparklingscience.urbantrees.security.ApiKeyFilter;

/**
 * Spring Security configuartion.
 * Authorizes requests only with appropriate api keys.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/26
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private ApiKeyFilter apiKeyFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
        	.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        	.and()
	        	.authorizeRequests()
	            .anyRequest().authenticated()
	        .and()
	        .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
            .csrf().disable();

    }

}
