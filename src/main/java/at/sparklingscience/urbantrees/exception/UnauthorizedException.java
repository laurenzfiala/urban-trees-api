package at.sparklingscience.urbantrees.exception;

import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;

/**
 * Exception class.
 * Used for exceptions where the client did not meet necessary auth requirements.
 * 
 * @author Laurenz Fiala
 * @since 2019/03/14
 */
public class UnauthorizedException extends GenericException {

	/**
	 * Serial of this exception. Should be updated on each change.
	 */
	private static final long serialVersionUID = 20190314L;
	
	/**
	 * Origin user of this exception (if they exist).
	 */
	private AuthenticationToken authToken;
	
	public UnauthorizedException(String message, AuthenticationToken authToken) {
		super(message);
		this.authToken = authToken;
	}
	
	@Override
	public String getMessage() {
		
		String msg = "";
		if (this.authToken != null) {
			msg += "[User ID: " + this.authToken.getId() +
				   ", User Name: " + this.authToken.getName() +
				   ", Auth ID: " + this.authToken.getAuthId() + "] ";
		}
		
		return msg + super.getMessage();
		
	}

	public AuthenticationToken getAuthToken() {
		return authToken;
	}

}
