package at.sparklingscience.urbantrees.exception;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Exception class.
 * Used for exceptions where the client sent too many requests
 * in a short amount of time and should wait before sending another one.
 * 
 * @author Laurenz Fiala
 * @since 2020/12/29
 */
public class TooManyRequestsException extends GenericException {

	/**
	 * Serial of this exception. Should be updated on each change.
	 */
	private static final long serialVersionUID = 20201229L;
	
	/**
	 * After what date the client is allowed to send requests again.
	 */
	private Instant retryAfter;
	
	public TooManyRequestsException(String message, Instant retryAfter) {
		super(message);
		this.retryAfter = retryAfter;
	}
	
	public TooManyRequestsException(String message, int retryAfterSeconds) {
		super(message);
		this.retryAfter = Instant.now().plus(retryAfterSeconds, ChronoUnit.SECONDS);
	}

	public TooManyRequestsException(String message, ClientError clientError) {
		super(message, clientError);
	}

	public Instant getRetryAfter() {
		return retryAfter;
	}

}
