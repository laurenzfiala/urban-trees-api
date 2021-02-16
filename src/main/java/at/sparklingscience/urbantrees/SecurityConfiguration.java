package at.sparklingscience.urbantrees;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.sparklingscience.urbantrees.controller.AdminController;
import at.sparklingscience.urbantrees.mapper.AuthMapper;
import at.sparklingscience.urbantrees.security.AdminAccessDecisionVoter;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationFilter;
import at.sparklingscience.urbantrees.security.authentication.apikey.ApiKeyFilter;
import at.sparklingscience.urbantrees.security.authentication.otk.TokenAuthenticationProvider;
import at.sparklingscience.urbantrees.security.authentication.otp.UserOtpAuthenticationProvider;
import at.sparklingscience.urbantrees.security.authentication.user.UserAuthenticationProvider;
import at.sparklingscience.urbantrees.security.authorization.JWTAuthorizationFilter;
import at.sparklingscience.urbantrees.service.AuthenticationService;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Spring Security configuartion.
 * Configures security of the whole application.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/26
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	/**
	 * Duration after which a generated JWT token expires.
	 * In milliseconds.
	 */
	public static final long 				JWT_EXPIRATION_TIME				= 86_400_000; /* 24 hours */
	
	/**
	 * Duration after which a generated login link expire.
	 * In milliseconds.
	 */
	public static final long				LOGIN_LINK_EXPIRATION_TIME		= 86_400_000; /* 24 hours */
	
	/**
	 * Maximum allowed age of token to access admin
	 * resources (those needing ROLE_ADMIN).
	 */
	public static final long				ADMIN_MAX_TOKEN_AGE				= 1_800_000; /* 30 minutes */

	/**
	 * Header key for the JWT token.
	 */
	public static final String 				HEADER_KEY 						= "Authorization";
	
	/**
	 * Prefix for the JWT token header value.
	 */
	public static final String 				JWT_TOKEN_PREFIX				= "Bearer ";

	/**
	 * Claims key for the user id.
	 */
	public static final String 				JWT_CLAIMS_USERID_KEY			= "uid";

	/**
	 * Claims key for the authentication id.
	 */
	public static final String 				JWT_CLAIMS_AUTHID_KEY			= "aid";
	
	/**
	 * Claims key for the users' roles.
	 */
	public static final String 				JWT_CLAIMS_ROLES_KEY			= "rol";
	
	/**
	 * Algorithm to use when signing the JWT token.
	 */
	public static final SignatureAlgorithm	JWT_AUTHENTICATION_SIG_ALG 		= SignatureAlgorithm.HS512;
	
	/**
	 * Role which is granted the privilege to access functionality of {@link AdminController}.
	 * "ROLE_" is automatically prepended by spring.
	 */
	public static final String ADMIN_ACCESS_ROLE = "ADMIN";
	
	/**
	 * Role needed for standard users.
	 * "ROLE_" is automatically prepended by spring.
	 */
	public static final String USER_ACCESS_ROLE = "USER";

	/**
	 * The role ID for users authenticated with an API key.
	 * "ROLE_" is automatically prepended by spring.
	 */
	public static final String API_KEY_ACCESS_ROLE = "USER_APIKEY";
	
	/**
	 * Role needed for phenology observations.
	 * "ROLE_" is automatically prepended by spring.
	 */
	public static final String PHENOLOGY_OBSERVATION_ACCESS_ROLE = "PHENOBS";
	
	/**
	 * Role granted only temporarily to change users' password.
	 * "ROLE_" is automatically prepended by spring.
	 */
	public static final String TEMPORARY_CHANGE_PASSWORD_ACCESS_ROLE = "TEMP_CHANGE_PASSWORD";
	
	/**
	 * Size of one-time login key in bytes.
	 */
	public static final int SECURE_LOGIN_KEY_BYTES = 64;
	
	/**
	 * Length of permissions PIN (digits).
	 */
	public static final int PERMISSIONS_PIN_LENGTH = 6;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private AuthMapper authMapper;
	
	@Autowired
	private AuthenticationService authService;
	
	@Autowired
	private ObjectMapper jsonObjectMapper;
	
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
		    			"/beacon",
		    			"/beacon/*",
		    			"/beacon/*/data",
		    			"/content/**"
    			).permitAll()
		    	.antMatchers(
		    			"/admin/**"
    			).hasRole(ADMIN_ACCESS_ROLE)
		    	.antMatchers(
		    			"/beacon/**"
    			).hasAnyRole(API_KEY_ACCESS_ROLE, USER_ACCESS_ROLE)
		    	.antMatchers(
		    			"/account/changepassword"
    			).hasAnyRole(USER_ACCESS_ROLE, TEMPORARY_CHANGE_PASSWORD_ACCESS_ROLE)
		    	.antMatchers(
		    			"/tree/**/phenology",
		    			"/user/phenology/**"
    			).hasAnyRole(API_KEY_ACCESS_ROLE, USER_ACCESS_ROLE)
		    	.antMatchers(
		    			"/**"
    			).hasAnyRole(USER_ACCESS_ROLE)
		    	.anyRequest().authenticated()
		    	.accessDecisionManager(this.accessDecisionManager())
	    	.and()
		    	.sessionManagement()
		    	.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	    	.and()
	    		.addFilterBefore(new ApiKeyFilter(this.authenticationManager(), this.authMapper), UsernamePasswordAuthenticationFilter.class)
	    		.addFilter(new AuthenticationFilter(this.authenticationManager(), this.authService, this.jsonObjectMapper))
                .addFilter(new JWTAuthorizationFilter(this.authenticationManager(), this.authService));

    }
    
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
    	
        auth.authenticationProvider(this.userAuthProvider())
        	.authenticationProvider(this.userOtpAuthProvider())
        	.authenticationProvider(this.tokenAuthProvider());
        
    }
    
    @Bean
    public AuthenticationProvider userAuthProvider() {
    	UserAuthenticationProvider provider = new UserAuthenticationProvider();
    	provider.setUserDetailsService(this.userDetailsService);
    	provider.setPasswordEncoder(this.bCryptPasswordEncoder());
        return provider;
    }
    
    @Bean
    public AuthenticationProvider userOtpAuthProvider() {
    	UserOtpAuthenticationProvider provider = new UserOtpAuthenticationProvider();
    	provider.setUserDetailsService(this.userDetailsService);
    	provider.setPasswordEncoder(this.bCryptPasswordEncoder());
        return provider;
    }  

    @Bean
    public AuthenticationProvider tokenAuthProvider() {
    	TokenAuthenticationProvider provider = new TokenAuthenticationProvider();
    	provider.setUserDetailsService(this.userDetailsService);
        return provider;
    }
    
	@Bean
    public AccessDecisionManager accessDecisionManager() {
		List<AccessDecisionVoter<? extends Object>> decisionVoters =
    			Arrays.<AccessDecisionVoter<? extends Object>>asList(
				  new WebExpressionVoter(),
				  new AdminAccessDecisionVoter(),
				  new RoleVoter(),
				  new AuthenticatedVoter()
				);
      return new UnanimousBased(decisionVoters);
    }
    
	/**
	 * NOTE:
	 * There is anyother instance of this encoder in {@link AuthenticationService}
	 * which des not use this bean.
	 * @see AuthenticationService#bCryptPasswordEncoder
	 */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
}
