package at.sparklingscience.urbantrees.domain;

/**
 * Holds a single content access entry with an amount of affected content
 * entries (whether these are approvable, viewable, etc. depends on the
 * context).
 * 
 * @author Laurenz Fiala
 * @since 2022/03/07
 */
public class UserContentManagerAccess {

	private UserContentAccess access;
	
	private int affectedContentAmount;

	public UserContentManagerAccess(UserContentAccess access, int affectedContentAmount) {
		this.access = access;
		this.affectedContentAmount = affectedContentAmount;
	}

	public UserContentAccess getAccess() {
		return access;
	}

	public void setAccess(UserContentAccess access) {
		this.access = access;
	}

	public int getAffectedContentAmount() {
		return affectedContentAmount;
	}

	public void setAffectedContentAmount(int affectedContentAmount) {
		this.affectedContentAmount = affectedContentAmount;
	}
	
}
