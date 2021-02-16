package at.sparklingscience.urbantrees.exception;

public class CmsElementDeserializationException extends RuntimeException {
	
	/**
	 * Serial of this exception. Should be updated on each change.
	 */
	private static final long serialVersionUID = 20201228L;

	public CmsElementDeserializationException(String message) {
		super(message);
	}

	public CmsElementDeserializationException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
