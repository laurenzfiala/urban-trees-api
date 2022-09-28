package at.sparklingscience.urbantrees.domain;

/**
 * Simple context for a {@link UserLevelAction}.
 * This means, a soft association with a specific ID that's the reason for the level action.
 * 
 * @author Laurenz Fiala
 * @since 2021/05/12
 */
public class UserLevelActionContext {
	
	/**
	 * Empty context which does not match anything.
	 */
	public static UserLevelActionContext EMPTY = new UserLevelActionContext();

	/**
	 * Context version.
	 */
	private final long version = 20220913L;
	
	/**
	 * Associated ID, uniquely identifying the source action.
	 * Meaning depends on {@link UserLevelAction}.
	 */
	private String assocId = null;
	
	/**
	 * Common ID, which is used to check if XP may be applied again.
	 * Meaning depends on {@link UserLevelAction}.
	 */
	private String commonId = null;
	
	public UserLevelActionContext() {}
	
	public UserLevelActionContext(String assocId, String commonId) {
		this.assocId = assocId;
		this.commonId = commonId;
	}
	
	public UserLevelActionContext(long assocId, long commonId) {
		this(String.valueOf(assocId), String.valueOf(commonId));
	}

	public String getCommonId() {
		return commonId;
	}

	public String getAssocId() {
		return assocId;
	}

	public long getVersion() {
		return version;
	}
	
	@Override
	public String toString() {
		return "UserLevelActionContext[version=" + version +", assocId=" + assocId + "]";
	}
	
	public boolean hasInCommon(UserLevelActionContext context) {
		if (this.commonId == null) {
			return false;
		}
		if (this.commonId.equals(context.commonId)) {
			return true;
		}
		return false;
	}
	
	public boolean hasNotInCommon(UserLevelActionContext context) {
		return !this.hasInCommon(context);
	}
	
}
