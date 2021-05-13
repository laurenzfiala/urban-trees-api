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
	private final long version = 20210512L;
	
	/**
	 * Associated ID, uniquely identifying the source action.
	 * Meaning depends on {@link UserLevelAction}.
	 */
	private long assocId = -1;
	
	/**
	 * Common ID, which is used to check if XP may be applied again.
	 * Meaning depends on {@link UserLevelAction}.
	 */
	private long commonId = -1;
	
	public UserLevelActionContext() {}
	
	public UserLevelActionContext(long assocId, long commonId) {
		this.assocId = assocId;
		this.commonId = commonId;
	}

	public long getAssocId() {
		return assocId;
	}

	public long getVersion() {
		return version;
	}

	public long getCommonId() {
		return commonId;
	}
	
	@Override
	public String toString() {
		return "UserLevelActionContext[version=" + version +", assocId=" + assocId + "]";
	}
	
}
