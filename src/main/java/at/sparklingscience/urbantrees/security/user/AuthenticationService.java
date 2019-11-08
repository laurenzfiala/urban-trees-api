package at.sparklingscience.urbantrees.security.user;

import java.nio.charset.StandardCharsets;
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
import at.sparklingscience.urbantrees.domain.UserIdentity;
import at.sparklingscience.urbantrees.domain.UserLight;
import at.sparklingscience.urbantrees.domain.UserPermission;
import at.sparklingscience.urbantrees.mapper.AuthMapper;
import at.sparklingscience.urbantrees.security.AuthSettings;
import at.sparklingscience.urbantrees.security.CryptoHelper;
import at.sparklingscience.urbantrees.service.UserService;

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
	private CryptoHelper cryptor;
	
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
		
		return this.decryptUser(this.authMapper.findUserById(userId));
		
	}
	
	/**
	 * Searches for a user with the given username.
	 * @param username Username (raw)
	 * @return If the user is found, return that user; if not, null will be returned.
	 */
	@Transactional
	public User findUser(final String usernameRaw) {
		final String encryptedUsername = this.encryptUsername(usernameRaw);
		return this.decryptUser(
				this.authMapper.findUserByUsername(encryptedUsername)
				);
		
	}
	
	/**
	 * Searches for a user with the given login key.
	 * @param username Username
	 * @return If the user is found, return that user; if not, null will be returned.
	 */
	@Transactional
	public User findUserByLoginKey(final String token) {
		
		return this.decryptUser(this.authMapper.findUserByLoginKey(token));
		
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
	 * @param usernameRaw Username to register (raw).
	 * @param rawPassword Password entered by the user (raw).
	 * @param roles Roles to assign to the new user.
	 * @return The given user object with ID set.
	 */
	@Transactional
	public User registerUser(final String usernameRaw, final String rawPassword, final List<Role> roles) {
		
		User newUser = new User();
		newUser.setUsername(this.encryptUsername(usernameRaw));
		
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
	public boolean changeUsername(final int userId, final String newUsernameRaw) {
		
		final String newUsernameEncrypted = this.encryptUsername(newUsernameRaw);
		final int updatedRows = this.authMapper.updateUsername(userId, newUsernameEncrypted);
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
	    this.authMapper.updateUserLoginKey(userId, secureToken);
	    
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
	public void increaseFailedLoginAttempts(final String usernameRaw) {
		
		this.authMapper.increaseFailedLoginAttemptsByUsername(
			this.encryptUsername(usernameRaw)
		);
		
	}
	
	@Transactional
	public void updateLastLoginAttemptDat(final String usernameRaw) {
		
		this.authMapper.updateLastLoginAttemptDatByUsername(
			this.encryptUsername(usernameRaw)
		);
		
	}
	
	@Transactional
	public void successfulAuth(final int userId) {
		
		this.authMapper.resetFailedLoginAttempts(userId);
		this.authMapper.updateLastLoginDat(userId);
		this.authMapper.updateUserLoginKey(userId, null);
		
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
		List<UserIdentity> grantingUsers = this.authMapper.findUserIdentitiesGrantingPermission(receivingUserId, permission.name());
		for (UserIdentity u : grantingUsers) {
			u.setUsername(this.cryptor.decrypt(u.getUsername()));
		}
		return grantingUsers;
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
		
		List<UserLight> users = this.authMapper.findAllUsersLight();
		for (UserLight u : users) {
			this.decryptUser(u);
		}
		return users;
		
	}
	
	/**
	 * Fetches all user roles available.
	 */
	public List<Role> getAllUserRoles() {
		
		return this.authMapper.findAllUserRoles();
		
	}
	
	/**
	 * Fetches the JWT secret from the database, used for
	 * signing the JWT tokens.
	 * DB-call is cached by mybatis.
	 */
	public byte[] getJWTSecret() {
		
		return this.authMapper.findSetting(AuthSettings.JWT_SECRET).getBytes(StandardCharsets.UTF_8);
		
	}
	
	/**
	 * Gets the encryption salt for queryable text from the auth settings table.
	 * DB-call is cached by mybatis.
	 */
	public String getQueryableEncryptionSalt() {
		
		return this.authMapper.findSetting(AuthSettings.QUERYABLE_ENCRYPTION_SALT);
		
	}
	
	/**
	 * Decrypt encrypted values of {@link User} object
	 * that has just been retrieved from the DB.
	 * 
	 * @param user user to be modified (no copy is created)
	 * @return the same object-ref as user-parameter, for convenience
	 */
	private User decryptUser(User user) {
		
		user.setUsername(this.cryptor.decrypt(user.getUsername()));
		
		return user;
		
	}

	/**
	 * Encrypt raw username and return it.
	 * 
	 * @param usernameRaw cleartext username
	 * @return encrypted username
	 */
	private String encryptUsername(String usernameRaw) {
		
		return this.cryptor.encryptQueryable(usernameRaw);
		
	}
	
}
