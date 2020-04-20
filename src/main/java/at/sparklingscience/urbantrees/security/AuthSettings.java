package at.sparklingscience.urbantrees.security;

/**
 * Keys for access_data.settings table.
 * Important Note: MyBatis is configured to cache settings and flush its cache every 15 minutes.
 * 
 * @author Laurenz Fiala
 * @since 2018/06/14
 */
public enum AuthSettings {

	MAX_LOGIN_ATTEMPTS,
	TIMEOUT_LOGIN_ATTEMPTS_SEC;
	
	@Override
	public String toString() {
		return this.name();
	}
	
}
