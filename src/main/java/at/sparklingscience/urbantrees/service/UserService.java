package at.sparklingscience.urbantrees.service;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import at.sparklingscience.urbantrees.controller.util.ControllerUtil;
import at.sparklingscience.urbantrees.domain.UserLevelAction;
import at.sparklingscience.urbantrees.domain.UserLevelActionContext;
import at.sparklingscience.urbantrees.domain.UserPermission;
import at.sparklingscience.urbantrees.domain.UserXp;
import at.sparklingscience.urbantrees.mapper.UserMapper;

/**
 * Service for user-related actions..
 * 
 * @author Laurenz Fiala
 * @since 2019/01/17
 */
@Service
public class UserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
    private ApplicationService appService;

	@Autowired
    private AuthenticationService authService;
	
	@Autowired
    private UserMapper userMapper;
	
	/**
	 * Increase experience points for a single user, namely the one thats logged in.
	 * @param action action to get the xp amount from
	 * @param context optional context (why the XP have increased) object
	 * @param auth user authentication 
	 */
	public void increaseXp(UserLevelAction action, UserLevelActionContext context, Authentication auth) {
		
		if (ControllerUtil.isUserAnonymous(auth)) {
			return;
		}
		final int userId = ControllerUtil.getAuthToken(auth).getId();
		
		try {			
			this.increaseXp(action, new int[] {userId}, context);
		} catch (Throwable t) {
			LOGGER.error("Failed to increase XP for user: {}", t.getMessage(), t);
			this.appService.logExceptionEvent("Failed to increase XP for user: " + t.getMessage(), t);
		}
		
	}
	
	/**
	 * Increase xp for all given users.
	 * Every user gets the same amount of XP (action-xp divided by amount of users)
	 * if reward wait time is not active.
	 */
	public void increaseXp(UserLevelAction action,
			int[] userIds,
			UserLevelActionContext context,
			UserPermission permission,
			Authentication auth) {
	
		if (ControllerUtil.isUserAnonymous(auth)) {
			return;
		}
		if (userIds == null || permission == null) {
			throw new RuntimeException("increaseXp: userIds or permission is null");
		}
		
		LOGGER.debug(
				"Increasing XP for users - users: {}, action: {}",
				userIds,
				action
				);

		try {
			final int ownUserId = ControllerUtil.getAuthToken(auth).getId();
			
			userIds = Arrays.stream(userIds)
				.boxed()
				.filter(uid -> uid == ownUserId || this.authService.hasUserPermission(uid, ownUserId, permission))
				.mapToInt(i -> i)
				.toArray();
			
			this.increaseXp(action, userIds, context);

			LOGGER.debug(
					"XP successfully increased for users - users: {}, action: {}",
					userIds,
					action
					);
			
		} catch (Throwable t) {
			LOGGER.error("Failed to increase XP for user: {}", t.getMessage(), t);
			this.appService.logExceptionEvent("Failed to increase XP for user: " + t.getMessage(), t);
		}
		
	}
	
	/**
	 * Execute the XP increase on the DB.
	 * @throws JsonProcessingException if the context can't be written to a string
	 */
	private void increaseXp(UserLevelAction action, int[] userIds, UserLevelActionContext context)
			throws RuntimeException, JsonProcessingException {
		
		for (int userId : userIds) {
			List<UserXp> xpHistory = this.userMapper.findXpHistoryByUserId(userId);		
			int xpFull = action.getRewardXp(action, context, xpHistory);
			int xp = (int) Math.floor(xpFull / userIds.length);

			LOGGER.debug("Increasing XP for user - user: {}, action: {}, xp: {}, context: {}", userId, action, xp, context);
			if (this.userMapper.insertIncreaseLevel(xp, action, userId, context) == 0) {
				this.prepareXp(userId, true);
				this.userMapper.insertIncreaseLevel(xp, action, userId, context);
			}
			LOGGER.debug("Successfully increased XP for user - user: {}, action: {}, xp: {}, context: {}", userId, action, xp, context);
			
		}		
		
	}
	
	public void prepareXp(int userId) {
		this.prepareXp(userId, false);
	}
	
	public void prepareXp(int userId, boolean upgrade) {
		
		LOGGER.debug("insert initial XP for user - user: {}", userId);
		
		UserLevelAction action = UserLevelAction.INITIAL;
		if (upgrade) {
			action = UserLevelAction.UPGRADE_ACCOUNT;
		}
		
		try {
			this.userMapper.insertLevel(userId, action.getDefaultRewardXp(), action.toString(), null);			
		} catch (Throwable t) {
			LOGGER.error("Failed to insert XP for user {}: {}", userId, t.getMessage(), t);
			this.appService.logExceptionEvent("Failed to insert XP for user " + userId + ": " + t.getMessage(), t);
		}
		
		LOGGER.debug("XP successfully inserted for user - user: {}", userId);
		
	}

}
