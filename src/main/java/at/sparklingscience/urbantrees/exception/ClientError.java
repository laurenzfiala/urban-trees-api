package at.sparklingscience.urbantrees.exception;

/**
 * Holds all errors used to display correct and
 * accurate error messages in the UI.
 * 
 * @author Laurenz Fiala
 * @since 2018/03/03
 */
public enum ClientError {
	
	PHENOLOGY_DUPLICATE(1),
	
	PHENOLOGY_IMAGE_UPLOAD_NO_FILENAME(10),
	PHENOLOGY_IMAGE_UPLOAD_INVALID_TYPE(11);
	
	private final int errorCode;
	
	private ClientError(final int errorCode) {
		this.errorCode = errorCode;
	}
	
	@Override
	public String toString() {
		return String.valueOf(this.getErrorCode());
	}

	public int getErrorCode() {
		return errorCode;
	}	
	
}
