package at.sparklingscience.urbantrees.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import at.sparklingscience.urbantrees.controller.util.ControllerUtil;
import at.sparklingscience.urbantrees.domain.UserLevelAction;
import at.sparklingscience.urbantrees.domain.UserPermission;
import at.sparklingscience.urbantrees.mapper.UserMapper;
import at.sparklingscience.urbantrees.security.user.AuthenticationService;

/**
 * Service for user-related actions..
 * 
 * @author Laurenz Fiala
 * @since 2019/01/17
 */
@Service
public class UserService {
	
	private static Logger logger;
	
	@Autowired
    private ApplicationService appService;

	@Autowired
    private AuthenticationService authService;
	
	@Autowired
    private UserMapper userMapper;
	
	public UserService(Logger classLogger) {
		logger = classLogger;
	}
	
	/**
	 * Increase experience points for a single user, namely the one thats logged in.
	 * @param action action to get the xp amount from
	 * @param auth user authentication 
	 */
	public void increaseXp(UserLevelAction action, Authentication auth) {
		
		if (ControllerUtil.isUserAnonymous(auth)) {
			return;
		}
		final int userId = ControllerUtil.getAuthToken(auth).getId();
		
		logger.debug("increase XP for user - user: {}, action: {}, xp: {}", userId, action, action.getRewardXp());
		
		try {
			this.increaseXp(action, userId);
			
			logger.debug("successfully increased XP for user - user: {}, action: {}, xp: {}", userId, action, action.getRewardXp());
		} catch (Throwable t) {
			logger.error("Failed to increase XP for user: {}", t.getMessage(), t);
			this.appService.logExceptionEvent("Failed to increase XP for user: " + t.getMessage(), t);
		}
		
	}
	
	/**
	 * Increase xp for all given users.
	 * Every user gets the same amount of XP (action-xp divided by amount of users).
	 */
	public void increaseXp(UserLevelAction action, int[] userIds, UserPermission permission, Authentication auth) {
	
		if (ControllerUtil.isUserAnonymous(auth)) {
			return;
		}
		
		final int ownUserId = ControllerUtil.getAuthToken(auth).getId();
		final int singleUserXp = (int) Math.floor(action.getRewardXp() / userIds.length);
		logger.debug(
				"increase XP for users - users: {}, action: {}, xp: {}, single user xp: {}, permission: {}",
				userIds,
				action,
				action.getRewardXp(),
				singleUserXp,
				permission
				);
		
		if (userIds == null || permission == null) {
			throw new RuntimeException("increaseXp: userIds or permission is null");
		}

		try {
			
			for (int otherUserId : userIds) {
				if (otherUserId == ownUserId || this.authService.hasUserPermission(otherUserId, ownUserId, permission)) {
					this.increaseXp(action, singleUserXp, otherUserId);
				}
			}

			logger.debug(
					"XP successfully increased for users - users: {}, action: {}, xp: {}, single user xp: {}",
					userIds,
					action,
					action.getRewardXp(),
					singleUserXp
					);
			
		} catch (Throwable t) {
			logger.error("Failed to increase XP for user: {}", t.getMessage(), t);
			this.appService.logExceptionEvent("Failed to increase XP for user: " + t.getMessage(), t);
		}
		
	}
	
	/**
	 * Increase the users' XP using the given action.
	 */
	public void increaseXp(UserLevelAction action, int userId) throws RuntimeException {
		this.increaseXp(action, action.getRewardXp(), userId);
	}
	
	/**
	 * Execute the XP increase on the DB.
	 */
	public void increaseXp(UserLevelAction action, int xp, int userId) throws RuntimeException {
		
		if (this.userMapper.insertIncreaseLevel(xp, action, userId) == 0) {
			this.prepareXp(userId, true);
			this.userMapper.insertIncreaseLevel(xp, action, userId);
		}
		
	}
	
	public void prepareXp(int userId) {
		this.prepareXp(userId, false);
	}
	
	public void prepareXp(int userId, boolean upgrade) {
		
		logger.debug("insert initial XP for user - user: {}", userId);
		
		UserLevelAction action = UserLevelAction.INITIAL;
		if (upgrade) {
			action = UserLevelAction.UPGRADE_ACCOUNT;
		}
		
		try {
			this.userMapper.insertLevel(userId, action.getRewardXp(), action.toString());			
		} catch (Throwable t) {
			logger.error("Failed to insert XP for user {}: {}", userId, t.getMessage(), t);
			this.appService.logExceptionEvent("Failed to insert XP for user " + userId + ": " + t.getMessage(), t);
		}
		
		logger.debug("XP successfully inserted for user - user: {}", userId);
		
	}

}
