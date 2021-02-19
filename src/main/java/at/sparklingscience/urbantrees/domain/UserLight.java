package at.sparklingscience.urbantrees.domain;

import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;

/**
 * A user without password.
 * 
 * Note: The restricted fields are still there, but the getters
 * 		 and setters are stubs.
 * 
 * @author Laurenz Fiala
 * @since 2019/01/29
 */
public class UserLight extends User {
	
	private static final long serialVersionUID = 20190129L;
	
	/**
	 * Whether the users' account is unlocked or locked.
	 */
	@NotNull(groups = {ValidationGroups.Read.class})
	private boolean isNonLocked;

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public void setPassword(String password) {}

	public boolean isNonLocked() {
		return isNonLocked;
	}

	public void setNonLocked(boolean isNonLocked) {
		this.isNonLocked = isNonLocked;
	}

}
