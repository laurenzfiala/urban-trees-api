package at.sparklingscience.urbantrees;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionVoter;

import at.sparklingscience.urbantrees.controller.AdminController;
import at.sparklingscience.urbantrees.security.AdminAccessDecisionVoter;
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
public class SecurityConfiguration {
	
	/**
	 * Duration after which a generated JWT token expires.
	 * In milliseconds.
	 */
	public static final long 				JWT_EXPIRATION_TIME				= 432_000_000; /* 5 days */
	
	/**
	 * Duration after which a generated login link expire.
	 * In milliseconds.
	 */
	public static final long				LOGIN_LINK_EXPIRATION_TIME		= 2_592_000_000l; /* 30 days */
	
	/**
	 * Maximum allowed age of token to access admin
	 * resources (those needing ROLE_ADMIN).
	 */
	public static final long				ADMIN_MAX_TOKEN_AGE				= 3_600_000; /* 1 hour */

	/**
	 * Key for the JWT token in cookies.
	 */
	public static final String 				JWT_COOKIE_KEY 					= "jwt_access_token";

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
	 * Role which is used to notify the user that their authentication
	 * method did not meet the requirements for admins.
	 * "ROLE_" is automatically prepended by spring.
	 */
	public static final String ADMIN_LOCKED_ACCESS_ROLE = "ADMIN_LOCKED";
	
	/**
	 * Role for users that need access to more system data than is default.
	 * "ROLE_" is automatically prepended by spring.
	 */
	public static final String ALL_DATA_ACCESS_ROLE = "ALL_DATA";
	
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
	 * Role granted only temporarily when no password is set.
	 * This is used to let the frontend know whether to require the
	 * old password or not.
	 * "ROLE_" is automatically prepended by spring.
	 */
	public static final String TEMPORARY_NO_PASSWORD_ACCESS_ROLE = "TEMP_NO_PASSWORD";
	
	/**
	 * Role granted only temporarily when logged in via login link.
	 * This is used to let the frontend know what info to show on forced logout or credentials change.
	 * "ROLE_" is automatically prepended by spring.
	 */
	public static final String TEMPORARY_LOGIN_LINK_ACCESS_ROLE = "TEMP_LOGIN_LINK";
	
	/**
	 * Role granted only temporarily to only allow the user
	 * activation of their two-factor authentication.
	 * "ROLE_" is automatically prepended by spring.
	 */
	public static final String TEMPORARY_ACTIVATE_OTP_ACCESS_ROLE = "TEMP_ACTIVATE_OTP";
	
	/**
	 * Size of one-time login key in bytes.
	 */
	public static final int SECURE_LOGIN_KEY_BYTES = 64;
	
	/**
	 * Length of permissions PIN (digits).
	 */
	public static final int PERMISSIONS_PIN_LENGTH = 6;
	
	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
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
		    			"/content",
		    			"/content/*",
		    			"/content/file/*"
				).permitAll()
		    	/*.antMatchers( XXX uncomment when you want to allow anonymous CMS content editing
		    			HttpMethod.POST,
		    			"/content",
		    			"/content/file"
				).permitAll()*/
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
		    			"/account/otp/activate"
				).hasAnyRole(USER_ACCESS_ROLE, TEMPORARY_ACTIVATE_OTP_ACCESS_ROLE)
		    	.antMatchers(
		    			"/tree/**/phenology",
		    			"/user/phenology/**"
				).hasAnyRole(API_KEY_ACCESS_ROLE, USER_ACCESS_ROLE)
		    	.antMatchers(
		    			"/manage/**"
				).authenticated()
		    	.anyRequest().authenticated()
		    	.accessDecisionManager(this.accessDecisionManager())
			.and()
		    	.sessionManagement()
		    	.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	    	.and()
	    		.apply(AuthConfiguration.authConfig());
        return http.build();
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
	 * There is another instance of this encoder in {@link AuthenticationService}
	 * which des not use this bean.
	 * @see AuthenticationService#bCryptPasswordEncoder
	 */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
}
