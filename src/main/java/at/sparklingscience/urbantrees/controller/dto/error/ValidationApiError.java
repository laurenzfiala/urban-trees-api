package at.sparklingscience.urbantrees.controller.dto.error;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;

/**
 * Error returned from the API with multiple validation errors.
 * 
 * @see ApiError
 * @author Laurenz Fiala
 * @since 2022/06/12
 */
public class ValidationApiError extends ApiError {
	
	/**
	 * Holds multiple validation errors.
	 */
	private List<ObjectError> errors = new ArrayList<>();
   
	public ValidationApiError(HttpStatus status) {
		super(status);
	}

	public ValidationApiError(HttpStatus status, Throwable ex) {
		super(status, ex);
	}

	public ValidationApiError(HttpStatus status, String message, Throwable ex) {
		super(status, message, ex);
	}

	public List<ObjectError> getErrors() {
		return errors;
	}

	public void setErrors(List<ObjectError> errors) {
		this.errors = errors;
	}
	
}
