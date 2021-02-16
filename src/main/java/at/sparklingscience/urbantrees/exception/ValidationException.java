package at.sparklingscience.urbantrees.exception;

import org.springframework.validation.Errors;

/**
 * Exception class.
 * Used for exceptions where the client sent illegal or invalid data
 * and we may want to return detailed info on what fields were
 * invalid.
 * 
 * @author Laurenz Fiala
 * @since 2021/02/03
 */
public class ValidationException extends GenericException {

	/**
	 * Serial of this exception. Should be updated on each change.
	 */
	private static final long serialVersionUID = 20210203L;
	
	/**
	 * Errors that lead to this exception.
	 */
	private Errors errors;
	
	public ValidationException(String message, Errors errors) {
		super(message);
		this.errors = errors;
		this.setClientError(ClientError.FAILED_VALIDATION);
	}

	public Errors getErrors() {
		return errors;
	}	

}
