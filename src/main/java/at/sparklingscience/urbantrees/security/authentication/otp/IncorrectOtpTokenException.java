package at.sparklingscience.urbantrees.security.authentication.otp;

import at.sparklingscience.urbantrees.security.authentication.IncorrectTokenException;

/**
 * Exception class.
 * Used for exceptions where the client sent insufficient credentials
 * to the server.
 * This means no OTP was given and must be supplied by the client.
 * 
 * @author Laurenz Fiala
 * @since 2020/04/23
 */
public class IncorrectOtpTokenException extends IncorrectTokenException {

	/**
	 * Serial of this exception. Should be updated on each change.
	 */
	private static final long serialVersionUID = 20200423L;
	
	public IncorrectOtpTokenException(String message) {
		super(message);
	}
	
	public String flag() {
		return "OTP";
	}

}
