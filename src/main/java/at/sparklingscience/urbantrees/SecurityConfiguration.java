package at.sparklingscience.urbantrees;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import at.sparklingscience.urbantrees.mapper.AuthMapper;
import at.sparklingscience.urbantrees.security.apikey.ApiKeyFilter;
import at.sparklingscience.urbantrees.security.jwt.JWTAuthenticationFilter;
import at.sparklingscience.urbantrees.security.jwt.JWTAuthorizationFilter;
import io.jsonwebtoken.SignatureAlgorithm;

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
	
	/**
	 * Duration after which a generated JWT token expires.
	 * In milliseconds.
	 */
	public static final long 				JWT_EXPIRATION_TIME				= 86_400_000;

	/**
	 * Header key for the JWT token.
	 */
	public static final String 				HEADER_KEY 						= "Authorization";
	
	/**
	 * Prefix for the JWT token header value.
	 */
	public static final String 				JWT_TOKEN_PREFIX				= "Bearer ";
	
	/**
	 * Algorithm to use when signing the JWT token.
	 */
	public static final SignatureAlgorithm	JWT_AUTHENTICATION_SIG_ALG 		= SignatureAlgorithm.HS512;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private AuthMapper authMapper;
	
    @Override
    protected void configure(HttpSecurity http) throws Exception {

    	http
				.csrf().disable()
				.cors()
			.and()
		    	.authorizeRequests()
		    	.antMatchers(
		    			HttpMethod.GET,
		    			"/tree",
		    			"/tree/**",
		    			"/ui",
		    			"/ui/**",
		    			"/beacon/**/data"
    			).permitAll()
		    	.anyRequest().authenticated()
	    	.and()
		    	.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	    	.and()
	    		.addFilterBefore(new ApiKeyFilter(this.authenticationManager(), this.authMapper), UsernamePasswordAuthenticationFilter.class)
	    		.addFilter(new JWTAuthenticationFilter(this.authenticationManager(), this.authMapper))
                .addFilter(new JWTAuthorizationFilter(this.authenticationManager(), this.authMapper));

    }
    
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
	        .userDetailsService(this.userDetailsService)
	        .passwordEncoder(this.bCryptPasswordEncoder());
    }
    
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
}
