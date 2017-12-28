package at.sparklingscience.urbantrees.security;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.stereotype.Component;

import at.sparklingscience.urbantrees.mapper.AuthMapper;

/**
 * Custom filter for authentication of api keys through the database.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/26
 */
@Component
public final class ApiKeyFilter extends AbstractAuthenticationProcessingFilter  {

	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyFilter.class);
	
	/**
	 * HTTP Header key to get the api key from.
	 */
	private static final String API_KEY_HEADER = "x-api-key";
	
	/**
	 * Mybatis database mapper for authentication purposes.
	 * @see {@link AuthMapper}
	 */
	@Autowired
	private AuthMapper authMapper;
	
	/**
	 * Sets the affected resource paths ('/**' means all should be checked).
	 * Also sets a dummy {@link NoOpAuthenticationManager}.
	 * @see AbstractAuthenticationProcessingFilter
	 */
	protected ApiKeyFilter() {
		super("/**");
		setAuthenticationManager(new NoOpAuthenticationManager());
	}

	/**
	 * Custom logic to use for checking valid api keys.
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws org.springframework.security.core.AuthenticationException, IOException, ServletException {
		
		try {
			
			ApiKeyAuthentication authentication = this.extractApiKey((HttpServletRequest) request);
			LOGGER.trace("Attempting authentication for key " + authentication.getCredentials());
			
			UUID uuid = (UUID) authentication.getCredentials();
			authentication.setAuthenticated(authMapper.hasValidApiKey(uuid) > 0);
			
			return authentication;
			
		} catch (IllegalArgumentException | NullPointerException e) {
			LOGGER.debug("Authentication failed: " + e.getMessage());
			return new ApiKeyAuthentication(false);			
		}
		
	}
	
	/**
	 * Upon successful authentication, set the valid {@link ApiKeyAuthentication} object
	 * to be picked up by spring security.
	 */
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		SecurityContextHolder.getContext().setAuthentication(authResult);
	    chain.doFilter(request, response);
	}
    
    /**
     * Extracts the api key from the http request header {@value #API_KEY_HEADER}.
     * @param request Recieved {@link HttpServletRequest}
     * @return The {@link Authentication} object {@link ApiKeyAuthentication} containing the api key as its credentials.
     * @throws IllegalArgumentException if an illegal api key was found in the http headers
     * @throws NullPointerException if no api key was found in the http headers
     */
    private ApiKeyAuthentication extractApiKey(HttpServletRequest request) 
    		throws IllegalArgumentException, NullPointerException {
    	
    	final String apiKey = request.getHeader(API_KEY_HEADER);
    	return new ApiKeyAuthentication(UUID.fromString(apiKey));
    	
    }
    
}