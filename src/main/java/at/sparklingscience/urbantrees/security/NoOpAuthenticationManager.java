package at.sparklingscience.urbantrees.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import at.sparklingscience.urbantrees.security.authentication.apikey.ApiKeyFilter;

/**
 * {@link AuthenticationManager} which does nothing. This is needed
 * for the custom security filter implemented in {@link ApiKeyFilter}.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/26
 */
public class NoOpAuthenticationManager implements AuthenticationManager {

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		return null;
	}

}
