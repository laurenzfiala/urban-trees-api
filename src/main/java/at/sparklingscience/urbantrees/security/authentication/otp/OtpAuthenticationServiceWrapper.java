package at.sparklingscience.urbantrees.security.authentication.otp;

import at.sparklingscience.urbantrees.service.AuthenticationService;

/**
 * Wrapper for auth service used in OTP auth provider to decouple
 * them and prevent usage of authService methods in the auth procider.
 * 
 * @author Laurenz Fiala
 * @since 2020/04/23
 */
public class OtpAuthenticationServiceWrapper {
	
	private AuthenticationService authService;
	
	public OtpAuthenticationServiceWrapper(AuthenticationService authService) {
		this.authService = authService;
	}
	
	public void validateOtp(final int userId, final String inputCode) throws OtpValidationException {
		this.authService.validateOtp(userId, inputCode);
	}

	public AuthenticationService getAuthService() {
		return authService;
	}

	public void setAuthService(AuthenticationService authService) {
		this.authService = authService;
	}

}
