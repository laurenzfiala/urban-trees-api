package at.sparklingscience.urbantrees.domain;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Holds all achievements for a user.
 * 
 * @author Laurenz Fiala
 * @since 2019/01/15
 */
public class UserAchievements {

	/**
	 * Latest XP of the user.
	 */
	@Min(0)
	private int xp;

	/**
	 * Previous XP of the user.
	 */
	@Min(0)
	private int lastXp;
	
	/**
	 * Users' experience points history.
	 */
	@NotNull
	private List<UserXp> xpHistory;

	public int getXp() {
		return xp;
	}

	public void setXp(int xp) {
		this.xp = xp;
	}

	public int getLastXp() {
		return lastXp;
	}

	public void setLastXp(int lastXp) {
		this.lastXp = lastXp;
	}

	public List<UserXp> getXpHistory() {
		return xpHistory;
	}

	public void setXpHistory(List<UserXp> xpHistory) {
		this.xpHistory = xpHistory;
	}

}
