package at.sparklingscience.urbantrees.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception class.
 * Used for exceptions where the client sent the wrong
 * credentials to the server (e.g. no OTP was given).
 * 
 * @author Laurenz Fiala
 * @since 2020/04/23
 */
public abstract class IncorrectTokenException extends AuthenticationException {

	/**
	 * Serial of this exception. Should be updated on each change.
	 */
	private static final long serialVersionUID = 20200423L;
	
	public IncorrectTokenException(String message) {
		super(message);
	}
	
	public abstract String flag();

}
