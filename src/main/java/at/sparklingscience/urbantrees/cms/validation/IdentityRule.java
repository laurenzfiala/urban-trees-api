package at.sparklingscience.urbantrees.cms.validation;

import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.cms.CmsContent;
import at.sparklingscience.urbantrees.cms.UserContentConfiguration;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;

/**
 * Create a new rule which allowes only certain users to save a
 * {@link CmsContent}.
 * @author Laurenz Fiala
 * @since 2021/12/13
 */
public class IdentityRule implements Rule {
	
	/**
	 * @see Evaluator
	 */
	private Evaluator evaluator;
	
	/**
	 * Create a new identity rule.
	 * @param evaluator function to evaluate if a given path may be saved
	 * 					by the current user
	 * @see IdentityRule
	 */
	public IdentityRule(Evaluator evaluator) {
		
		this.evaluator = evaluator;
		
	}

	@Override
	public void check(@NotNull String contentPath,
					  @NotNull AuthenticationToken authToken,
					  @NotNull CmsContent cmsContent,
					  @NotNull SimpleErrors errors) {
		
		try {
			if (!this.evaluator.evaluate(contentPath, authToken)) {
				errors.reject("Given content path may not be saved by this user.");
			}			
		} catch (RuntimeException e) {
			errors.reject("Given content path is invalid.");
		}
		
	}
	
	/**
	 * Evaluators determine whether the given user is allowed to
	 * save content at the given path.
	 * Use for lambdas in {@link UserContentConfiguration}.
	 */
	public interface Evaluator {
		boolean evaluate(String path, AuthenticationToken auth);
	}
	
}
