package at.sparklingscience.urbantrees.security.authentication.user;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

import at.sparklingscience.urbantrees.security.authentication.jwt.JWTAuthenticationToken;
import at.sparklingscience.urbantrees.security.authentication.otk.TokenAuthenticationProvider;
import at.sparklingscience.urbantrees.security.authentication.otp.IncorrectOtpTokenException;
import at.sparklingscience.urbantrees.security.authentication.otp.UserOtpAuthenticationProvider;
import at.sparklingscience.urbantrees.security.user.User;
import at.sparklingscience.urbantrees.security.user.UserDetailsService;

/**
 * Authenticates a user by their username and password.
 * 
 * @see UserOtpAuthenticationProvider
 * @see TokenAuthenticationProvider
 * @author Laurenz Fiala
 * @since 2020/04/23
 */
public class UserAuthenticationProvider extends DaoAuthenticationProvider {

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Assert.isInstanceOf(UserAuthenticationToken.class, authentication,
				() -> messages.getMessage(
						"AbstractUserDetailsAuthenticationProvider.onlySupports",
						"Only UserAuthenticationToken is supported"));
		
		User user = (User) ((UserDetailsService) this.getUserDetailsService()).loadUserByUsername(
				((UserAuthenticationToken) authentication).getName()
				);
		
		if (user == null) {
			throw new AuthenticationCredentialsNotFoundException("Token invalid.");
		}
		if (!user.isEnabled()) {
			throw new DisabledException("Account is locked.");
		}
		if (!user.isAccountNonLocked()) {
			throw new LockedException("Account is temporarily locked.");
		}
		
		final String rawPassword = String.valueOf(authentication.getCredentials());
		final boolean passwordMatches = super.getPasswordEncoder().matches(rawPassword, user.getPassword());
		if (!passwordMatches) {
			throw new BadCredentialsException("Password invalid.");
		}
		
		if (UserAuthenticationToken.class.equals(authentication.getClass()) && user.isUsingOtp()) {
			throw new IncorrectOtpTokenException("Invalid login token given."); // OTP missing
		}
		
		JWTAuthenticationToken authToken = new JWTAuthenticationToken(
				user.getId(),
				user.getUsername(),
				user.getAuthorities()
			);
		authToken.setDetails(user);
		
		return authToken;
		
	}
	
	@Override
	public boolean supports(Class<?> authentication) {
		return UserAuthenticationToken.class.equals(authentication);
	}
	
}
