package at.sparklingscience.urbantrees.security.authentication.otp;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

import at.sparklingscience.urbantrees.security.authentication.jwt.JWTAuthenticationToken;
import at.sparklingscience.urbantrees.security.authentication.otk.TokenAuthenticationProvider;
import at.sparklingscience.urbantrees.security.authentication.user.UserAuthenticationProvider;
import at.sparklingscience.urbantrees.security.user.UserDetailsService;

/**
 * Authenticates a user by their username, password and OTP.
 * We reuse the logic from {@link UserAuthenticationProvider}, but before we return
 * the auth token, we also check the OTP.
 * 
 * @see UserAuthenticationProvider
 * @see TokenAuthenticationProvider
 * @author Laurenz Fiala
 * @since 2020/04/23
 */
public class UserOtpAuthenticationProvider extends UserAuthenticationProvider {

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Assert.isInstanceOf(UserOtpAuthenticationToken.class, authentication,
				() -> messages.getMessage(
						"AbstractUserDetailsAuthenticationProvider.onlySupports",
						"Only UserOtpAuthenticationToken is supported"));
		
		UserOtpAuthenticationToken authToken = (UserOtpAuthenticationToken) authentication;
		JWTAuthenticationToken userAuth = (JWTAuthenticationToken) super.authenticate(authentication);
		
		try {
			((UserDetailsService) super.getUserDetailsService()).validateUserOtp(userAuth.getId(), authToken.getOtp());
		} catch (OtpValidationException e) {
			throw new BadCredentialsException("Invalid OTP.");
		}
		
		return userAuth;
		
	}
	
	@Override
	public boolean supports(Class<?> authentication) {
		return UserOtpAuthenticationToken.class.isAssignableFrom(authentication);
	}
	
}
