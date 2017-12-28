package at.sparklingscience.urbantrees.exception;

/**
 * Exception class.
 * Used for exceptions where the client sent illegal or invalid data.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/28
 */
public class BadRequestException extends GenericException {

	/**
	 * Serial of this exception. Should be updated on each change.
	 */
	private static final long serialVersionUID = 20171228L;

	public BadRequestException(String message) {
		super(message);
	}

}
