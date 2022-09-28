package at.sparklingscience.urbantrees;

import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.sparklingscience.urbantrees.mapper.AuthMapper;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationFilter;
import at.sparklingscience.urbantrees.security.authentication.apikey.ApiKeyFilter;
import at.sparklingscience.urbantrees.security.authentication.otk.TokenAuthenticationProvider;
import at.sparklingscience.urbantrees.security.authentication.otp.UserOtpAuthenticationProvider;
import at.sparklingscience.urbantrees.security.authentication.user.UserAuthenticationProvider;
import at.sparklingscience.urbantrees.security.authorization.JWTAuthorizationFilter;
import at.sparklingscience.urbantrees.security.user.UserDetailsService;
import at.sparklingscience.urbantrees.service.AuthenticationService;

/**
 * Configures {@link HttpSecurity} to use custom authentication
 * providers and filters.
 * 
 * @author Laurenz Fiala
 * @since 2022/09/05
 */
public class AuthConfiguration extends AbstractHttpConfigurer<AuthConfiguration, HttpSecurity> {

	@Override
    public void configure(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        
        var jsonObjectMapper = context.getBean(ObjectMapper.class);
        var authMapper = context.getBean(AuthMapper.class);
        var authService = context.getBean(AuthenticationService.class);
        var passwordEncoder = context.getBean(BCryptPasswordEncoder.class);
        var userDetailsService = context.getBean(UserDetailsService.class);
        
        var apiKeyFilter = new ApiKeyFilter(authenticationManager, authMapper);
        var authenticationFilter = new AuthenticationFilter(authenticationManager, authService, jsonObjectMapper);
        var jwtAuthorizationFilter = new JWTAuthorizationFilter(authenticationManager, authService);
        
        var userAuthProvider = new UserAuthenticationProvider();
        userAuthProvider.setUserDetailsService(userDetailsService);
        userAuthProvider.setPasswordEncoder(passwordEncoder);
        
        var userOtpAuthProvider = new UserOtpAuthenticationProvider();
        userOtpAuthProvider.setUserDetailsService(userDetailsService);
        userOtpAuthProvider.setPasswordEncoder(passwordEncoder);
        
        var tokenAuthProvider = new TokenAuthenticationProvider();
        tokenAuthProvider.setUserDetailsService(userDetailsService);
        tokenAuthProvider.setPasswordEncoder(passwordEncoder);
        
        http.addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
        	.addFilter(authenticationFilter)
        	.addFilter(jwtAuthorizationFilter)
        	.authenticationProvider(userAuthProvider)
        	.authenticationProvider(userOtpAuthProvider)
        	.authenticationProvider(tokenAuthProvider);
        
    }
	
	public static AuthConfiguration authConfig() {
		return new AuthConfiguration();
	}
	
}
