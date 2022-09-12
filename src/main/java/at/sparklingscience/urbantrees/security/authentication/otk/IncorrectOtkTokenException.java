package at.sparklingscience.urbantrees.security.authentication.otk;

import at.sparklingscience.urbantrees.security.authentication.IncorrectTokenException;

/**
 * Exception class.
 * Used for exceptions where the client sent insufficient credentials
 * to the server.
 * This means no PIN for the secure login key was given, but must
 * be supplied by the client.
 * 
 * @author Laurenz Fiala
 * @since 2022/09/12
 */
public class IncorrectOtkTokenException extends IncorrectTokenException {

	/**
	 * Serial of this exception. Should be updated on each change.
	 */
	private static final long serialVersionUID = 202220912L;
	
	public IncorrectOtkTokenException(String message) {
		super(message);
	}
	
	public String flag() {
		return "OTK";
	}

}
