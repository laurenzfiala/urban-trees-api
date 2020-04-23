package at.sparklingscience.urbantrees.security.authentication.otk;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

import at.sparklingscience.urbantrees.security.authentication.jwt.JWTAuthenticationToken;
import at.sparklingscience.urbantrees.security.user.User;
import at.sparklingscience.urbantrees.security.user.UserDetailsService;

/**
 * Authenticates a user by their login link token.
 * 
 * @author Laurenz Fiala
 * @since 2019/02/04
 */
public class TokenAuthenticationProvider extends DaoAuthenticationProvider {

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Assert.isInstanceOf(TokenAuthenticationToken.class, authentication,
				() -> messages.getMessage(
						"AbstractUserDetailsAuthenticationProvider.onlySupports",
						"Only TokenAuthenticationToken is supported"));
		
		User user = (User) ((UserDetailsService) this.getUserDetailsService()).loadUserByLoginKey(
				((TokenAuthenticationToken) authentication).getToken()
				);
		
		if (user == null) {
			throw new BadCredentialsException("Token invalid.");
		}
		
		JWTAuthenticationToken authToken = new JWTAuthenticationToken(
				user.getId(),
				user.getUsername(),
				user.getAuthorities(),
				user
			);
		
		return authToken;
		
	}
	
	@Override
	public boolean supports(Class<?> authentication) {
		return TokenAuthenticationToken.class.isAssignableFrom(authentication);
	}
	
}
