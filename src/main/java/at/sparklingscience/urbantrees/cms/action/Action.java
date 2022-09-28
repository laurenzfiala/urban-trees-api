package at.sparklingscience.urbantrees.cms.action;

import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.UserContent;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;

/**
 * An action that is applied for configured contentPaths after a content
 * is saved.
 * @author Laurenz Fiala
 * @since 2022/03/22
 */
public interface Action {
	
	/**
	 * Execute the action of the implementor.
	 * @param userContent content inserted
	 * @param user 
	 */
	void doAction(@NotNull UserContent userContent,
				  @NotNull AuthenticationToken authToken);
	
}
