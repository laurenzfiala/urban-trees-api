package at.sparklingscience.urbantrees.security.user;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import at.sparklingscience.urbantrees.SecurityConfiguration;
import at.sparklingscience.urbantrees.domain.Role;
import at.sparklingscience.urbantrees.domain.User;
import at.sparklingscience.urbantrees.mapper.AuthMapper;
import at.sparklingscience.urbantrees.security.AuthSettings;
import at.sparklingscience.urbantrees.service.UserService;

/**
 * Service provides functionalits for authorization,
 * e.g. register users, etc.
 * 
 * @author Laurenz Fiala
 * @since 2018/06/10
 */
@Service
public class AuthenticationService {
	
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private AuthMapper authMapper;
	
	@Autowired
	private UserService userService;
	
	/**
	 * Searches for a user with the given username.
	 * @param userId Users' id.
	 * @return If the user is found, return that user; if not, null will be returned.
	 */
	@Transactional
	public User findUser(final int userId) {
		
		return this.authMapper.findUserById(userId);
		
	}
	
	/**
	 * Searches for a user with the given username.
	 * @param username Username
	 * @return If the user is found, return that user; if not, null will be returned.
	 */
	@Transactional
	public User findUser(final String username) {
		
		return this.authMapper.findUserByUsername(username);
		
	}
	
	/**
	 * Searches for a user with the given login key.
	 * @param username Username
	 * @return If the user is found, return that user; if not, null will be returned.
	 */
	@Transactional
	public User findUserByLoginKey(final String token) {
		
		return this.authMapper.findUserByLoginKey(token);
		
	}
	
	/**
	 * Un-expire the given users' credentials.
	 */
	@Transactional
	public void unexpireCredentials(final int userId) {
		
		this.authMapper.updateCredentialsNonExpired(userId, true);
		
	}
	
	/**
	 * Expire the given users' credentials.
	 */
	@Transactional
	public void expireCredentials(final int userId) {
		
		this.authMapper.updateCredentialsNonExpired(userId, false);
		
	}

	/**
	 * Set given user active.
	 */
	@Transactional
	public void activate(final int userId) {
		
		this.authMapper.updateActive(userId, true);
		
	}
	
	/**
	 * Set given user inactive.
	 */
	@Transactional
	public void inactivate(final int userId) {
		
		this.authMapper.updateActive(userId, false);
		
	}
	
	/**
	 * Inserts a new user into the database.
	 * @param username Username to register.
	 * @param rawPassword Password entered by the user (raw).
	 * @param roles Roles to assign to the new user.
	 * @return The given user object with ID set.
	 */
	@Transactional
	public User registerUser(final String username, final String rawPassword, final List<Role> roles) {
		
		User newUser = new User();
		newUser.setUsername(username);
		
		if (rawPassword == null) {
			newUser.setPassword(null);
		} else {
			newUser.setPassword(this.bCryptPasswordEncoder.encode(rawPassword));
		}
		
		this.authMapper.insertUser(newUser);
		if (roles != null && roles.size() > 0) {
			this.authMapper.insertUserRoles(newUser.getId(), roles);
		}
		this.userService.prepareXp(newUser.getId());
		
		return newUser;
		
	}

	/**
	 * Update a users' password to newPassword
	 * if oldPassword matches the previously stored password.
	 * @param userId id of user to update
	 * @param oldPassword Previous password
	 * @param newPassword New password to be stored
	 * @param changeWithoutOldPw If true, don't check the previous password for
	 * 							 validity.
	 * @return true if the password change was successful, false otherwise
	 */
	@Transactional
	public boolean changePassword(final int userId, final String oldPassword, final String newPassword, final boolean changeWithoutOldPw) {
		
		final User user = this.authMapper.findUserById(userId);
		if (user == null) {
			LOGGER.error("Given user not found by id. Please investigate, this should be handled internally.");
			return false;
		}
		
		boolean oldPasswordMatches;
		if (changeWithoutOldPw || user.getPassword() == null) {
			oldPasswordMatches = true;
		} else {
			oldPasswordMatches = this.bCryptPasswordEncoder.matches(oldPassword, user.getPassword());
		}
		final String hashedNewPassword = this.bCryptPasswordEncoder.encode(newPassword);
		
		if (oldPasswordMatches) {
			final int updatedRows = this.authMapper.updateUserPassword(userId, hashedNewPassword);
			return updatedRows == 1;
		}
		
		LOGGER.info("Given old password did not match database. Password not updated.");
		return false;
		
	}

	/**
	 * Update a users' username to newUsername.
	 * @param userId userId
	 * @param newUsername new username
	 * @return true if successful; false otherwise
	 */
	@Transactional
	public boolean changeUsername(final int userId, final String newUsername) {
		
		final int updatedRows = this.authMapper.updateUsername(userId, newUsername);
		if (updatedRows == 1) {
			return true;
		} else {
			LOGGER.info("Failed to update usename for unknown reason. Please investigate.");
			return false;
		}
		
	}

	/**
	 * Check if given user is locked based on login attempts
	 * and last login date.
	 * @param user User to check.
	 * @return true if account is unlocked; false if account is locked.
	 */
	public boolean isUserNonLocked(User user) {
		
		final int maxLoginAttempts = 
				Integer.parseInt(this.authMapper.findSetting(AuthSettings.MAX_LOGIN_ATTEMPTS));
		final int timeoutLoginAttemptsSec =
				Integer.parseInt(this.authMapper.findSetting(AuthSettings.TIMEOUT_LOGIN_ATTEMPTS_SEC));
		if (user.getFailedloginAttempts() >= maxLoginAttempts
			&& System.currentTimeMillis() - timeoutLoginAttemptsSec * 1000 <= user.getLastLoginAttemptDate().getTime()) {
			return false;
		} else {
			return true;
		}
		
	}
	
	@Transactional
	public String getLoginKey(int userId) {
		
		String storedToken = this.authMapper.findUserLoginKey(userId);
		if (storedToken != null) {
			return storedToken;
		}
		
		final String secureToken = this.generateSecureToken();
	    this.authMapper.updateUserLoginKey(userId, secureToken);
	    
	    return secureToken;
		
	}
	
	public String generateSecureToken() {
		
		Random random = new SecureRandom();
		byte[] randomData = new byte[SecurityConfiguration.SECURE_LOGIN_KEY_BYTES];
		random.nextBytes(randomData);
		
		Encoder encoder = Base64.getUrlEncoder().withoutPadding();
	    return encoder.encodeToString(randomData);
		
	}
	
	@Transactional
	public void modifyRoles(int userId, List<Role> roles, boolean isAdd) {
		
		if (isAdd) {
			this.authMapper.insertUserRoles(userId, roles);
		} else {
			this.authMapper.deleteUserRoles(userId, roles);
		}
		
	}
	
}
