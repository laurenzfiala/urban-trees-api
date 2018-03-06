package at.sparklingscience.urbantrees.exception;

/**
 * Exception class.
 * Used for exceptions where an entity could not be found.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/26
 */
public class NotFoundException extends GenericException {

	/**
	 * Serial of this exception. Should be updated on each change.
	 */
	private static final long serialVersionUID = 20171226L;
	
	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(String message, ClientError clientError) {
		super(message, clientError);
	}

}
