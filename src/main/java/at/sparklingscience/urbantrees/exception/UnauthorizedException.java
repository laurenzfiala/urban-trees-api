package at.sparklingscience.urbantrees.exception;

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
	
	public UnauthorizedException(String message) {
		super(message);
	}

	public UnauthorizedException(String message, ClientError clientError) {
		super(message, clientError);
	}

}
