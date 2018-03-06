package at.sparklingscience.urbantrees.exception;

/**
 * Exception class.
 * Used for exceptions where the server encountered an unexpected exception.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/28
 */
public class InternalException extends GenericException {

	/**
	 * Serial of this exception. Should be updated on each change.
	 */
	private static final long serialVersionUID = 20171228L;

	public InternalException(String message) {
		super(message);
	}
	
	public InternalException(String message, ClientError clientError) {
		super(message, clientError);
	}

}
