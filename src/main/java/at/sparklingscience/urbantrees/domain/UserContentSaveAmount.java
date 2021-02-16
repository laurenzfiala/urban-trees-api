package at.sparklingscience.urbantrees.domain;

import java.util.Date;

/**
 * Holds information on how many user
 * content entries are found.
 * 
 * @author Laurenz Fiala
 * @since 2020/12/29
 */
public class UserContentSaveAmount {
	
	private long amount;
	
	private Date minSaveDate;

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public Date getMinSaveDate() {
		return minSaveDate;
	}

	public void setMinSaveDate(Date minSaveDate) {
		this.minSaveDate = minSaveDate;
	}

}
