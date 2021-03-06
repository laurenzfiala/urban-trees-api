package at.sparklingscience.urbantrees.exception;

/**
 * Exception class.
 * Used as baseline for most exception classes in this project.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/26
 */
public abstract class GenericException extends RuntimeException {

	/**
	 * Serial of this exception. Should be incremented on each change.
	 */
	private static final long serialVersionUID = 20171226L;
	
	/**
	 * Description of exception cause.
	 */
	private String message;
	
	/**
	 * @see ClientError
	 */
	private ClientError clientError;
	
	public GenericException(String message) {
		this(message, null, null);
	}
	
	public GenericException(String message, Throwable cause) {
		this(message, null, cause);
	}
	
	public GenericException(String message, ClientError clientError) {
		this(message, clientError, null);
	}
	
	public GenericException(String message, ClientError clientError, Throwable cause) {
		this.setMessage(message);
		this.setClientError(clientError);
		this.initCause(cause);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ClientError getClientError() {
		return clientError;
	}

	public void setClientError(ClientError clientError) {
		this.clientError = clientError;
	}

}
