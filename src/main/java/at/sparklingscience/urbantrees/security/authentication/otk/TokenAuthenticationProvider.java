package at.sparklingscience.urbantrees.security.authentication.otk;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import at.sparklingscience.urbantrees.SecurityConfiguration;
import at.sparklingscience.urbantrees.security.SecurityUtil;
import at.sparklingscience.urbantrees.security.authentication.jwt.JWTAuthenticationToken;
import at.sparklingscience.urbantrees.security.user.User;
import at.sparklingscience.urbantrees.security.user.UserDetailsService;

/**
 * Authenticates a user by their login link token.
 * Note: Users authenticated by a OTK may not be admins, so we remove ROLE_ADMIN from the users' authorities.
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
		
		final List<GrantedAuthority> grantedAuthorities = user.getAuthorities()
				.stream()
				.filter((a) -> !a.getAuthority().equals(SecurityUtil.role(SecurityConfiguration.ADMIN_ACCESS_ROLE)))
				.collect(Collectors.toList());
		
		JWTAuthenticationToken authToken = new JWTAuthenticationToken(
				user.getId(),
				user.getUsername(),
				grantedAuthorities,
				user
			);
		
		return authToken;
		
	}
	
	@Override
	public boolean supports(Class<?> authentication) {
		return TokenAuthenticationToken.class.isAssignableFrom(authentication);
	}
	
}
