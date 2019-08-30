package at.sparklingscience.urbantrees.domain;

import java.io.Serializable;

/**
 * Various user data shown throughout the UI.
 * 
 * @author Laurenz Fiala
 * @since 2018/06/09
 */
public class UserData implements Serializable {

	private static final long serialVersionUID = 20190729L;

	/**
	 * Users' unique identifier.
	 */
	private int newMessagesAmount;

	public int getNewMessagesAmount() {
		return newMessagesAmount;
	}

	public void setNewMessagesAmount(int newMessagesAmount) {
		this.newMessagesAmount = newMessagesAmount;
	}

}
