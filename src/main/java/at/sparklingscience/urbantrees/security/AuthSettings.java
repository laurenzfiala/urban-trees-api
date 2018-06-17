package at.sparklingscience.urbantrees.security;

/**
 * Keys for access_data.settings table.
 * 
 * @author Laurenz Fiala
 * @since 2018/06/14
 */
public enum AuthSettings {

	JWT_SECRET;
	
	@Override
	public String toString() {
		return this.name();
	}
	
}
