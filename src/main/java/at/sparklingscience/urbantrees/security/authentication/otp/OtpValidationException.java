package at.sparklingscience.urbantrees.security.authentication.otp;

/**
 * Exception class.
 * Used for exceptions where the OTP-validation failed.
 * 
 * @author Laurenz Fiala
 * @since 2020/04/22
 */
public class OtpValidationException extends Exception {

	/**
	 * Serial of this exception. Should be updated on each change.
	 */
	private static final long serialVersionUID = 20200422L;
	
	public OtpValidationException(String message) {
		super(message);
	}
	
	public OtpValidationException(String message, Throwable cause) {
		super(message, cause);
	}

}
