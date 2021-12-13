package at.sparklingscience.urbantrees.security.authentication.otp;

import at.sparklingscience.urbantrees.security.authentication.user.UserAuthenticationToken;

/**
 * An authentication token holding the entered credentials (including an OTP).
 * 
 * @author Laurenz Fiala
 * @since 2020/04/23
 */
public class UserOtpAuthenticationToken extends UserAuthenticationToken {

	private static final long serialVersionUID = 20200423L;

	private String otp;
	
	public UserOtpAuthenticationToken(String username, String password, String otp) {
		super(username, password);
		this.otp = otp;
	}

	public String getOtp() {
		return otp;
	}
	
}
