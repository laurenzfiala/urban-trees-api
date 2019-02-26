package at.sparklingscience.urbantrees.exception;

/**
 * Holds all errors used to display correct and
 * accurate error messages in the UI.
 * 
 * @author Laurenz Fiala
 * @since 2018/03/03
 */
public enum ClientError {
	
	GENERIC_ERROR(0),
	
	PHENOLOGY_DUPLICATE(10),
	
	PHENOLOGY_IMAGE_UPLOAD_NO_FILENAME(11),
	PHENOLOGY_IMAGE_UPLOAD_INVALID_TYPE(12),
	
	CITY_DUPLICATE(20),
	CITY_INTERNAL_ERROR(21),
	
	BEACON_DUPLICATE(30),
	BEACON_INTERNAL_ERROR(30),
	BEACON_DELETE_FAILED(31),
	
	TREE_INSERT_FAILED(40),
	TREE_UPDATE_FAILED(41),

	FAILED_KEY_STORE(50);

	
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
