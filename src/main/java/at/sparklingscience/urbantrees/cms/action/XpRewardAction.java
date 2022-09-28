package at.sparklingscience.urbantrees.cms.action;

import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.UserContent;
import at.sparklingscience.urbantrees.domain.UserContentStatus;
import at.sparklingscience.urbantrees.domain.UserLevelAction;
import at.sparklingscience.urbantrees.domain.UserLevelActionContext;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;
import at.sparklingscience.urbantrees.service.UserService;

/**
 * Create a new XP reward rule where the specified action is applied to the
 * content only when the content is APPROVED.
 * For XP insertion, the contentPath functions as commonId. This
 * means the timeout for the given action is per-contentPath.
 */
public class XpRewardAction implements Action {
	
	private UserService userService;
	
	private final UserLevelAction action;
	
	/**
	 * Create a new XP reward rule.
	 * @param userService user service for increasing XP
	 * @param action which action to be rewarded
	 */
	public XpRewardAction(UserService userService, UserLevelAction action) {

		this.userService = userService;
		this.action = action;
		
	}
	
	@Override
	public void doAction(@NotNull UserContent content,
						 @NotNull AuthenticationToken authToken) {
		
		if (content.getStatus() != UserContentStatus.APPROVED) {
			return;
		}
		
		this.userService.increaseXp(
			this.action,
			new UserLevelActionContext(String.valueOf(content.getId()), content.getContentPath()),
			new int[] {content.getUser().getId()}
		);
		
	}
	
}
