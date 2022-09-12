package at.sparklingscience.urbantrees.security.authentication;

/**
 * DTO for user credentials sent by the frontend.
 * 
 * @author Laurenz Fiala
 * @since 2020/04/23
 */
public class UserCredentials {

	/**
	 * Users' identifying username.
	 */
	private String username;

	/**
	 * Raw (unhashed) password.
	 */
	private String password;
	
	/**
	 * (optional) one-time password
	 */
	private String otp;

	/**
	 * (optional) login token
	 */
	private String secureLoginKey;

	/**
	 * (optional) additional PIN for OTK auth
	 */
	private String secureLoginKeyPin;
	
	public UserCredentials() {
	}

	public UserCredentials(String username,
						   String password,
						   String otp,
						   String secureLoginKey,
						   String secureLoginKeyPin) {
		this.username = username;
		this.password = password;
		this.otp = otp;
		this.secureLoginKey = secureLoginKey;
		this.secureLoginKeyPin = secureLoginKeyPin;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public String getSecureLoginKey() {
		return secureLoginKey;
	}

	public void setSecureLoginKey(String secureLoginKey) {
		this.secureLoginKey = secureLoginKey;
	}

	public String getSecureLoginKeyPin() {
		return secureLoginKeyPin;
	}

	public void setSecureLoginKeyPin(String secureLoginKeyPin) {
		this.secureLoginKeyPin = secureLoginKeyPin;
	}
	
}
