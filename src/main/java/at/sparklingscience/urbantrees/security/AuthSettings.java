package at.sparklingscience.urbantrees.security;

/**
 * Keys for access_data.settings table.
 * Important Note: MyBatis is configured to cache settings and flush its cache every 15 minutes.
 * 
 * @author Laurenz Fiala
 * @since 2018/06/14
 */
public enum AuthSettings {

	JWT_SECRET,
	MAX_LOGIN_ATTEMPTS,
	TIMEOUT_LOGIN_ATTEMPTS_SEC,
	
	/**
	 * Salt needs to be at least 8 bytes and must have even number of HEX chars.
	 */
	QUERYABLE_ENCRYPTION_SALT;
	
	@Override
	public String toString() {
		return this.name();
	}
	
}
