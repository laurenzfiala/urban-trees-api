package at.sparklingscience.urbantrees.security.user;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

import at.sparklingscience.urbantrees.security.jwt.AuthenticationToken;
import at.sparklingscience.urbantrees.security.jwt.TokenAuthenticationToken;
import at.sparklingscience.urbantrees.security.jwt.UserDetailsService;

/**
 * TODO
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
		
		AuthenticationToken authToken = new AuthenticationToken(
				user.getId(),
				user.getUsername(),
				user.getAuthorities()
			);
		authToken.setDetails(user);
		
		return authToken;
		
	}
	
	@Override
	public boolean supports(Class<?> authentication) {
		return TokenAuthenticationToken.class.isAssignableFrom(authentication);
	}
	
}
