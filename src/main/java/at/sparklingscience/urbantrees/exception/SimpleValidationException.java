package at.sparklingscience.urbantrees.exception;

import at.sparklingscience.urbantrees.cms.validation.SimpleErrors;

/**
 * Exception class.
 * Used for exceptions where the client sent illegal or invalid data
 * and we may want to return detailed info on what is invalid.
 * 
 * @author Laurenz Fiala
 * @since 2021/08/03
 */
public class SimpleValidationException extends GenericException {

	/**
	 * Serial of this exception. Should be updated on each change.
	 */
	private static final long serialVersionUID = 20210803L;
	
	/**
	 * Errors that lead to this exception.
	 */
	private SimpleErrors errors;
	
	public SimpleValidationException(String message, SimpleErrors errors) {
		super(message);
		this.errors = errors;
		this.setClientError(ClientError.FAILED_VALIDATION);
	}

	public SimpleErrors getErrors() {
		return errors;
	}
	
	@Override
	public String toString() {
		return super.toString() + "\n" + errors.toString();
	}

}
