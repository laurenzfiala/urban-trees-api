package at.sparklingscience.urbantrees.controller.dto.error;

import java.util.Date;
import org.springframework.http.HttpStatus;

import at.sparklingscience.urbantrees.exception.ClientError;

/**
 * Contains info about an error in the API as well as the HTTP status.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/26
 */
public class ApiError {

	/**
	 * Error status name.
	 */
	private HttpStatus status;
   
	/**
	 * Timestamp of the error event.
	 */
	private Date timestamp;
	
	/**
	 * Basic error description for the enduser.
	 */
	private String message;
   
	/**
	 * Converted to an errorCode, since we don't want
	 * the enum names in the response.
	 * @see ClientError
	 */
	private int clientErrorCode;

	protected ApiError() {
		timestamp = new Date();
	}

	public ApiError(HttpStatus status) {
		this();
		this.status = status;
	}

	public ApiError(HttpStatus status, Throwable ex) {
		this();
		this.status = status;
		this.message = "Unexpected error";
	}

	public ApiError(HttpStatus status, String message, Throwable ex) {
		this();
		this.status = status;
		this.message = message;
	}

	public HttpStatus getStatus() {
		return status;
	}
	
	public void setStatus(HttpStatus status) {
		this.status = status;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

	public int getClientErrorCode() {
		return clientErrorCode;
	}

	public void setClientErrorCodeFromClientError(ClientError clientError) {
		if (clientError != null) {
			this.clientErrorCode = clientError.getErrorCode();
		}
	}
	   
}
