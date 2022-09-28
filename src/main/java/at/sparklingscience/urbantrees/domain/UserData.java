package at.sparklingscience.urbantrees.domain;

import java.io.Serializable;

/**
 * Various user data shown throughout the UI.
 * 
 * @author Laurenz Fiala
 * @since 2018/06/09
 */
public class UserData implements Serializable {

	private static final long serialVersionUID = 20220928L;

	private int unresolvedReportsAmount;
	private int unapprovedContentAmount;

	public int getUnresolvedReportsAmount() {
		return unresolvedReportsAmount;
	}

	public void setUnresolvedReportsAmount(int unresolvedReportsAmount) {
		this.unresolvedReportsAmount = unresolvedReportsAmount;
	}

	public int getUnapprovedContentAmount() {
		return unapprovedContentAmount;
	}

	public void setUnapprovedContentAmount(int unapprovedContentAmount) {
		this.unapprovedContentAmount = unapprovedContentAmount;
	}
	
	/**
	 * Constructs a new empty user data instance and returns it.
	 * @return new {@link UserData} instance
	 */
	public static UserData empty() {
		return new UserData();
	}

}
