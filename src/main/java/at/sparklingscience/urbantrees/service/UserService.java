package at.sparklingscience.urbantrees.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import at.sparklingscience.urbantrees.controller.util.ControllerUtil;
import at.sparklingscience.urbantrees.domain.UserLevelAction;
import at.sparklingscience.urbantrees.mapper.UserMapper;

/**
 * Service for user-related actions..
 * 
 * @author Laurenz Fiala
 * @since 2019/01/17
 */
@Service
public class UserService {
	
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
    private UserMapper userMapper;
	
	/**
	 * TODO
	 * @param action
	 * @param principal
	 */
	public void increaseXp(UserLevelAction action, Authentication auth) {
		
		if (ControllerUtil.isUserAnonymous(auth)) {
			return;
		}
		
		final int userId = ControllerUtil.getAuthToken(auth).getId();
		LOGGER.debug("increase XP for user - user: {}, action: {}, xp: {}", userId, action, action.getRewardXp());
		
		this.increaseXp(action, userId);
		
	}
	
	public void increaseXp(UserLevelAction action, int userId) {
		
		LOGGER.debug("increase XP for user - user: {}, action: {}, xp: {}", userId, action, action.getRewardXp());
		
		try {
			if (this.userMapper.insertIncreaseLevel(action.getRewardXp(), action, userId) == 0) {
				this.prepareXp(userId, true);
				this.userMapper.insertIncreaseLevel(action.getRewardXp(), action, userId);
			}
		} catch (Throwable t) {
			LOGGER.error("Failed to increase XP for user: " + t.getMessage(), t);
		}
		
		LOGGER.debug("XP successfully increased for user - user: {}, action: {}, xp: {}", userId, action, action.getRewardXp());
		
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
			this.userMapper.insertLevel(userId, action.getRewardXp(), action.toString());			
		} catch (Throwable t) {
			LOGGER.error("Failed to insert XP for user: " + t.getMessage(), t);
		}
		
		LOGGER.debug("XP successfully inserted for user - user: {}", userId);
		
	}

}
