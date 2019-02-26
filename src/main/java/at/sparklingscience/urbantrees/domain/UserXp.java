package at.sparklingscience.urbantrees.domain;

import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Experience points object for achievements.
 * 
 * @author Laurenz Fiala
 * @since 2018/02/18
 */
public class UserXp {

	@Min(0)
	private int xp;
	
	@Min(1)
	private UserLevelAction action;
	
	@NotNull
	private Date date;

	public int getXp() {
		return xp;
	}

	public void setXp(int xp) {
		this.xp = xp;
	}

	public UserLevelAction getAction() {
		return action;
	}

	public void setAction(UserLevelAction action) {
		this.action = action;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
}
