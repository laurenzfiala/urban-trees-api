package at.sparklingscience.urbantrees.domain;

/**
 * Values of the action-column in user_data.level.
 * 
 * @author Laurenz Fiala
 * @since 2019/01/17
 */
public enum UserLevelAction {
	
	PHENOLOGY_OBSERVATION(200),
	PHENOLOGY_IMAGE_UPLOAD(150),
	BEACON_READOUT(100),
	UPGRADE_ACCOUNT(0),
	ADMIN_THANKYOU(null),
	INITIAL(0);
	
	private Integer rewardXp;
	
	private UserLevelAction(final Integer rewardXp) {
		this.rewardXp = rewardXp;
	}

	public int getRewardXp() {
		return rewardXp;
	}

}
