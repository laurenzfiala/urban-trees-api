package at.sparklingscience.urbantrees.domain;

import java.util.List;

/**
 * Interface for enum constants in {@link UserLevelAction}.
 * 
 * @author Laurenz Fiala
 * @since 2021/05/12
 */
public interface UserLevelActionInterface {
	
	/**
	 * Get default XP to be rewarded depending on the {@link UserLevelAction}.
	 * @return amount of xp to reward b default
	 */
	int getDefaultRewardXp();
	
	/**
	 * Calculates the reward xp given the parameters.
	 * The default impl uses {@link UserLevelAction#getDefaultWaitDuration()}
	 * to calculate the wait period and returns 0 if it still applies. If the
	 * wait period is over, {@link UserLevelAction#getDefaultRewardXp()} is
	 * returned.
	 * @param action action of xp reward to calulate
	 * @param context context of reward
	 * @param xpHistory xp reward hisotry of the user
	 * @return amount of xp to reward
	 */
	default int getRewardXp(UserLevelAction action,
			UserLevelActionContext context,
			List<UserXp> xpHistory) {
		boolean isAfter = UserLevelAction.isAfter(action, context, xpHistory, i -> i.plus(action.getDefaultWaitDuration()));
		return isAfter ? 0 : action.getDefaultRewardXp();
	}

}
