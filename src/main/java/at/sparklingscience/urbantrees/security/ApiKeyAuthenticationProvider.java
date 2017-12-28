package at.sparklingscience.urbantrees.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyAuthenticationProvider implements AuthenticationProvider {

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		
		if (!authentication.isAuthenticated()) {
			throw new ApiKeyAuthenticationException("API key is invalid");
		}
		
		return authentication;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return ApiKeyAuthentication.class.isAssignableFrom(authentication);
	}

}
