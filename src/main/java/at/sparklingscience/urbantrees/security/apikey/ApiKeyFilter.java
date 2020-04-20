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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import at.sparklingscience.urbantrees.SecurityConfiguration;
import at.sparklingscience.urbantrees.mapper.AuthMapper;
import at.sparklingscience.urbantrees.security.NoOpAuthenticationManager;
import at.sparklingscience.urbantrees.security.SecurityUtil;

/**
 * Custom filter for authentication of api keys through the database.
 * 
 * Sets the affected resource paths ('/**' means all should be checked). Also
 * sets a dummy {@link NoOpAuthenticationManager}.
 * 
 * @see AbstractAuthenticationProcessingFilter
 * @author Laurenz Fiala
 * @since 2017/12/26
 */
public final class ApiKeyFilter extends BasicAuthenticationFilter {
	
	private static Logger logger = LoggerFactory.getLogger(ApiKeyFilter.class); // TODO

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
	
	public ApiKeyFilter(AuthenticationManager authenticationManager, AuthMapper authMapper) {
		super(authenticationManager);
		this.authMapper = authMapper;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		logger.trace("Starting API key authentication.");

		final String apiKey = req.getHeader(ApiKeyFilter.API_KEY_HEADER);

		if (apiKey == null) {
			logger.trace("Skipping API key authentication, since header is null.");
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
        	logger.trace("API key valid.");
            return new PreAuthenticatedAuthenticationToken(
            		apiKeyUuid.toString(),
            		SecurityConfiguration.API_KEY_ACCESS_ROLE,
            		Collections.singletonList(SecurityUtil.grantedAuthority(SecurityConfiguration.API_KEY_ACCESS_ROLE))
    		);
        }
        logger.info("API key {} invalid.", apiKey);
        return null;
        
    }

}