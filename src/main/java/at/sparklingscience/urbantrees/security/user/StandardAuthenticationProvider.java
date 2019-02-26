package at.sparklingscience.urbantrees.security.user;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * TODO
 * @author Laurenz Fiala
 * @since 2019/02/04
 */
public class StandardAuthenticationProvider extends DaoAuthenticationProvider {

	@Override
	protected Authentication createSuccessAuthentication(Object principal, Authentication authentication,
			UserDetails user) {
		UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) super.createSuccessAuthentication(principal, authentication, user);
		auth.setDetails(user);
		return auth;
	}
	
}
