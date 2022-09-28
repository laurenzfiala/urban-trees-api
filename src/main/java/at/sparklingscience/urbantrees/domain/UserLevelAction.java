package at.sparklingscience.urbantrees.domain;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

/**
 * Values of the action-column in user_data.level.
 * 
 * @author Laurenz Fiala
 * @since 2019/01/17
 */
public enum UserLevelAction implements UserLevelActionInterface {
	
	PHENOLOGY_OBSERVATION(200) {
		@Override
		public int getRewardXp(UserLevelAction action, UserLevelActionContext context, List<UserXp> xpHistory) {
			boolean isAfter = isAfter(
					action,
					context,
					xpHistory,
					i -> ZonedDateTime.ofInstant(i, ZoneId.systemDefault())
									  .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
									  .truncatedTo(ChronoUnit.DAYS)
									  .toInstant()
			);
			return isAfter ? 0 : this.getDefaultRewardXp();
		}
	},
	PHENOLOGY_IMAGE_UPLOAD(150){
		@Override
		public int getRewardXp(UserLevelAction action, UserLevelActionContext context, List<UserXp> xpHistory) {
			boolean isAfter = isAfter(
					action,
					context,
					xpHistory,
					i -> ZonedDateTime.ofInstant(i, ZoneId.systemDefault())
									  .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
									  .truncatedTo(ChronoUnit.DAYS)
									  .toInstant()
			);
			return isAfter ? 0 : this.getDefaultRewardXp();
		}
	},
	USER_EXP_DAYS_SUBMIT(250, Duration.of(3650, ChronoUnit.DAYS)),
	USER_TREE_CONTENT_SUBMIT(250, Duration.of(5, ChronoUnit.DAYS)),
	BEACON_READOUT(100, Duration.of(5, ChronoUnit.DAYS)),
	UPGRADE_ACCOUNT(0),
	ADMIN_THANKYOU(null),
	INITIAL(0);

	private Integer defaultRewardXp;
	private Duration defaultWaitDuration;
	
	private UserLevelAction(final Integer defaultRewardXp) {
		this(defaultRewardXp, Duration.ZERO);
	}
	
	private UserLevelAction(final Integer defaultRewardXp, final Duration defaultWaitDuration) {
		this.defaultRewardXp = defaultRewardXp;
		this.defaultWaitDuration = defaultWaitDuration;
	}
	
	/**
	 * The default implementation to use by the enum constants.
	 * Returns true if the wait period still applies. The according check is
	 * performed by checker.
	 * @param action action type of xp entry to insert
	 * @param context context of xp entry to insert
	 * @param xpHistory users' previous XP entries
	 * @param checker is handed the {@link Instant} of the last {@link UserXp}
	 * 				  entry with same action type, more than 0 XP and the same
	 * 				  common id
	 * @return true if the last {@link UserXp} entry with the same action
	 * 		   type, more than 0 XP and the same common id is still in the
	 * 		   wait period; false if the wait period does not apply.
	 */
	public static boolean isAfter(UserLevelAction action,
			UserLevelActionContext context,
			List<UserXp> xpHistory,
			UserLevelActionXpChecker checker) {
		
		Optional<UserXp> lastSimilarUserXp = xpHistory.stream()
				.filter(x -> x.getAction() == action && x.getXp() > 0 && context.hasInCommon(x.getContext()))
				.findFirst();
		return lastSimilarUserXp.isPresent() &&
				checker.getXpTimeout(lastSimilarUserXp.get().getDate().toInstant()).isAfter(Instant.now());
		
	}

	public int getDefaultRewardXp() {
		return defaultRewardXp;
	}

	public Duration getDefaultWaitDuration() {
		return defaultWaitDuration;
	}

}
