package at.sparklingscience.urbantrees.exception;

/**
 * Holds all errors used to display correct and
 * accurate error messages in the UI.
 * 
 * @author Laurenz Fiala
 * @since 2018/03/03
 */
public enum ClientError {
	
	UNCAUGHT(-1),
	GENERIC_ERROR(0),
	
	PHENOLOGY_DUPLICATE(10),
	
	PHENOLOGY_IMAGE_UPLOAD_NO_FILENAME(11),
	PHENOLOGY_IMAGE_UPLOAD_INVALID_TYPE(12),
	
	CITY_DUPLICATE(20),
	CITY_INTERNAL_ERROR(21),
	
	BEACON_DUPLICATE(30),
	BEACON_INTERNAL_ERROR(31),
	BEACON_DELETE_FAILED(32),
	BEACON_LOG_SEVERITY_INVALID(33),
	BEACON_UPDATE_FAILED(34),
	
	TREE_INSERT_FAILED(40),
	TREE_UPDATE_FAILED(41),

	FAILED_KEY_STORE(50),
	
	SAME_USER_PERM_REQUEST(60),
	
	USERNAME_DUPLICATE(70),
	
	FAILED_VALIDATION(80),
	
	CONTENT_BASE_OUTDATED(100);

	
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
