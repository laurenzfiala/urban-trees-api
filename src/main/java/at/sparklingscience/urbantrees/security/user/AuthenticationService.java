package at.sparklingscience.urbantrees.security.user;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import at.sparklingscience.urbantrees.SecurityConfiguration;
import at.sparklingscience.urbantrees.domain.Role;
import at.sparklingscience.urbantrees.domain.User;
import at.sparklingscience.urbantrees.domain.UserIdentity;
import at.sparklingscience.urbantrees.domain.UserLight;
import at.sparklingscience.urbantrees.domain.UserPermission;
import at.sparklingscience.urbantrees.exception.UnauthorizedException;
import at.sparklingscience.urbantrees.mapper.AuthMapper;
import at.sparklingscience.urbantrees.security.AuthSettings;
import at.sparklingscience.urbantrees.service.UserService;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;

/**
 * Service provides functionality for authorization,
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
	 * @param username Username (raw)
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
	 * @param username Username to register (raw).
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
	 * @param newUsername new username (raw)
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
	 * Check the DB user againt the given raw password.
	 * @param user DB user (password is encrypted)
	 * @param rawPassword plaintext password to check
	 * @return true if the password matches; flese otherwise
	 */
	public boolean isPasswordValid(User user, String rawPassword) {
		return this.bCryptPasswordEncoder.matches(rawPassword, user.getPassword());
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

	/**
	 * Check whether the given PPIN is valid for the given user.
	 * @param userId users' id
	 * @param ppin permissions PIN
	 * @return true if PPIN matches, false otherwise.
	 */
	@Transactional
	public boolean isPermissionPINValid(final int userId, final String ppin) {
		this.authMapper.increasePermissionsPINAttempts(userId);
		return this.authMapper.hasPermissionsPIN(userId, ppin);
	}
	
	@Transactional
	public String getLoginKey(int userId) {
		
		String storedToken = this.authMapper.findUserLoginKey(userId);
		if (storedToken != null) {
			return storedToken;
		}
		
		final String secureToken = this.generateSecureToken();
	    this.authMapper.updateUserLoginKey(
    		userId,
    		secureToken,
    		new Date(System.currentTimeMillis() + SecurityConfiguration.LOGIN_LINK_EXPIRATION_TIME)
		);
	    
	    return secureToken;
		
	}
	
	private String generateSecureToken() {
		
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
	
	@Transactional
	public void increaseFailedLoginAttempts(final String username) {
		this.authMapper.increaseFailedLoginAttemptsByUsername(username);
	}
	
	@Transactional
	public void updateLastLoginAttemptDat(final String username) {
		this.authMapper.updateLastLoginAttemptDatByUsername(username);
	}
	
	@Transactional
	public void successfulAuth(final int userId) {
		
		this.authMapper.resetFailedLoginAttempts(userId);
		this.authMapper.updateLastLoginDat(userId);
		this.authMapper.updateUserLoginKey(userId, null, null);
		
	}
	
	@Transactional
	public void deleteUser(final int userId) {
		
		this.authMapper.deleteUser(userId);
		
	}

	/**
	 * Add a single user permission to receivingUser.
	 * @param grantingUserId User ID of permission granting user (giving permission)
	 * @param receivingUserId User ID of permission receiving user (getting permission)
	 * @param permission type of permission
	 */
	@Transactional
	public void addUserPermission(int grantingUserId, int receivingUserId, UserPermission permission) {
		this.authMapper.insertUserPermission(grantingUserId, receivingUserId, permission.name());
	}

	/**
	 * Whether the receivingUser has the given permission from the given grantingUser.
	 * @param grantingUserId User ID of permission granting user (giving permission)
	 * @param receivingUserId User ID of permission receiving user (getting permission)
	 * @param permission type of permission
	 */
	@Transactional
	public boolean hasUserPermission(int grantingUserId, int receivingUserId, UserPermission permission) {
		return this.authMapper.hasUserPermission(grantingUserId, receivingUserId, permission.name()) > 0;
	}

	/**
	 * Get all users that have given the receivingUser the specified permission
	 * @param receivingUserId User ID of permission receiving user (getting permission)
	 * @param permission type of permission
	 * @return list of users that have given the receivingUser the specified permission
	 */
	@Transactional
	public List<UserIdentity> getUsersGrantingPermission(int receivingUserId, UserPermission permission) {
		return this.authMapper.findUserIdentitiesGrantingPermission(receivingUserId, permission.name());
	}
	
	/**
	 * Set a new permissions PIN and return it.
	 * Also resets the attempts in the DB.
	 */
	@Transactional
	public String newPermissionsPIN(final int userId) {
		
		final SecureRandom sr = new SecureRandom();
		final int pinNum = sr.nextInt((int) Math.pow(10, SecurityConfiguration.PERMISSIONS_PIN_LENGTH));
		final String pin = String.format("%0" + SecurityConfiguration.PERMISSIONS_PIN_LENGTH + "d", pinNum);
		
		this.authMapper.setPermissionsPIN(userId, pin);
		
		return pin;
		
	}
	
	/**
	 * Fetches all users in a light version.
	 * Important Note: THIS METHOD MUST SOLELY BE CALLED BY AN ADMIN VIA ADMINCONTROLLER!
	 */
	public List<UserLight> getAllUsersLight() {
		return this.authMapper.findAllUsersLight();
	}
	
	/**
	 * Fetches all user roles available.
	 */
	public List<Role> getAllUserRoles() {
		
		return this.authMapper.findAllUserRoles();
		
	}
	
	/**
	 * Get the current JWT secret key for the given user.
	 * @param userId user's id
	 * @return {@link SecretKey}
	 * @throws WeakKeyException if the stored user secret is too weak for the used algorithm.
	 * @throws UnauthorizedException if the given user is not found, no secret is found, or the user is inactivated.
	 */
	public SecretKey getJWTSecret(final int userId) throws WeakKeyException, UnauthorizedException {
		
		final String base64Secret = this.authMapper.findUserTokenSecret(userId);
		if (base64Secret == null) {
			throw new UnauthorizedException("Could not find user's login token. (user id = " + userId + ")");
		}
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
		
	}
	
	/**
	 * Generate a new JWT secret for the given user and store it.
	 * @param user user to update (from teh security package)
	 * @return the stored secretkey if it was successfully generated and stored.
	 * @throws RuntimeException if storing the secret could not be stored.
	 */
	public SecretKey generateJWTSecret(at.sparklingscience.urbantrees.security.user.User user) {
		final SecretKey signingKey = Keys.secretKeyFor(SecurityConfiguration.JWT_AUTHENTICATION_SIG_ALG);
		final String storeKey = Encoders.BASE64.encode(signingKey.getEncoded());
		
		final int updatedUsers = this.authMapper.updateUserTokenSecret(user.getId(), storeKey);
		if (updatedUsers != 1) {
			throw new RuntimeException("Token secret could not be stored for user " + user.getId() + ", instead " + updatedUsers + " users have been updated.");
		}
		
		return signingKey;
	}
	
}
