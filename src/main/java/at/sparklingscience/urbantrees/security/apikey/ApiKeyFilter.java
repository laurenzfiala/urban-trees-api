package at.sparklingscience.urbantrees.security.apikey;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import at.sparklingscience.urbantrees.mapper.AuthMapper;
import at.sparklingscience.urbantrees.security.NoOpAuthenticationManager;

/**
 * Custom filter for authentication of api keys through the database.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/26
 */
public final class ApiKeyFilter extends BasicAuthenticationFilter {

	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyFilter.class);

	/**
	 * The role ID for users authenticated with an API key.
	 */
	public static final SimpleGrantedAuthority ROLE_API_KEY = new SimpleGrantedAuthority("USER_APIKEY");

	/**
	 * HTTP Header key to get the api key from.
	 */
	private static final String API_KEY_HEADER = "x-api-key";

	/**
	 * Mybatis database mapper for authentication purposes.
	 * 
	 * @see {@link AuthMapper}
	 */
	private AuthMapper authMapper;

	/**
	 * Sets the affected resource paths ('/**' means all should be checked). Also
	 * sets a dummy {@link NoOpAuthenticationManager}.
	 * 
	 * @see AbstractAuthenticationProcessingFilter
	 */
	public ApiKeyFilter(AuthenticationManager authenticationManager, AuthMapper authMapper) {
		super(authenticationManager);
		this.authMapper = authMapper;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		LOGGER.trace("Starting API key authentication.");

		final String apiKey = req.getHeader(ApiKeyFilter.API_KEY_HEADER);

		if (apiKey == null) {
			LOGGER.trace("Skipping API key authentication, since header is null.");
			chain.doFilter(req, res);
			return;
		}
		
		PreAuthenticatedAuthenticationToken authentication = this.getAuthentication(apiKey);
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(req, res);

	}
	
	/**
	 * Checks if the given API key is valid and if so,
	 * returns the {@link PreAuthenticatedAuthenticationToken}.
	 * @param apiKey The API key previously extracted from the request headers.
	 * @return The auth token if successfully authenticated; null otherwise.
	 */
	private PreAuthenticatedAuthenticationToken getAuthentication(final String apiKey) {
		
		UUID apiKeyUuid = (UUID) UUID.fromString(apiKey);
        if (authMapper.hasValidApiKey(apiKeyUuid) > 0) {
        	LOGGER.trace("API key valid.");
            return new PreAuthenticatedAuthenticationToken(apiKeyUuid.toString(), ROLE_API_KEY.getAuthority(), Collections.singletonList(ROLE_API_KEY));
        }
        LOGGER.info("API key {} invalid.", apiKey);
        return null;
        
    }

}