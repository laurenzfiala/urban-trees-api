package at.sparklingscience.urbantrees.domain.validator;

import javax.validation.groups.Default;

/**
 * Contains all available validation groups for the API.
 * 
 * Some fields like IDs are only used for Reads,
 * because they are the result of an update. In that case,
 * we need to differentiate between types of validations.
 * 
 * @author Laurenz Fiala
 * @since 2018/01/21
 */
public interface ValidationGroups {
	
	/**
	 * Validation group for read operations.
	 * This includes e.g. GET-requests.
	 */
	public interface Read extends Default {}
	
	/**
	 * Validation group for updates.
	 * This includes e.g. POST-requests.
	 */
	public interface Update extends Default {}
	
}
