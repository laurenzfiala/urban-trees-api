package at.sparklingscience.urbantrees.domain;

import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;

/**
 * A user for authentication without password.
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

	public void setPassword(String password) {}
	
	@Override
	public void setSecureLoginKey(String secureLoginKey) {}

	public boolean isNonLocked() {
		return isNonLocked;
	}

	public void setNonLocked(boolean isNonLocked) {
		this.isNonLocked = isNonLocked;
	}

}
