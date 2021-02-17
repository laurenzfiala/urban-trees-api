package at.sparklingscience.urbantrees.exception;

/**
 * Exception class.
 * Used when user registration fails because of duplicate usernames.
 * 
 * @author Laurenz Fiala
 * @since 2021/02/16
 */
public class DuplicateUsernameException extends Exception {

	/**
	 * Serial of this exception. Should be updated on each change.
	 */
	private static final long serialVersionUID = 20210216L;
	
	/**
	 * Username that was already existent.
	 */
	private final String username;
	
	public DuplicateUsernameException(Throwable cause, String username) {
		super(null, cause);
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

}
