package at.sparklingscience.urbantrees.domain;

import java.time.Instant;

/**
 * Used for lambdas in {@link UserLevelAction} and {@link UserLevelActionInterface}.
 * 
 * @author Laurenz Fiala
 * @since 2021/05/12
 */
public interface UserLevelActionXpChecker {
	
	/**
	 * Implementors should calculate the action timeout instant from the given action.
	 * For instance, if XP rewards should only be given once per day, return the start
	 * of the next day (or actionDate + 24 hours, depending on requirements).
	 * @param actionInstant instant at which an action has occurred 
	 * @return the timeout instant for the given action instant
	 */
	Instant getXpTimeout(Instant actionInstant);

}
