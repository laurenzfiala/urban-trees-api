package at.sparklingscience.urbantrees.service;

import java.security.SecureRandom;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import at.sparklingscience.urbantrees.SecurityConfiguration;
import at.sparklingscience.urbantrees.controller.util.ControllerUtil;
import at.sparklingscience.urbantrees.domain.OtpCredentials;
import at.sparklingscience.urbantrees.domain.Role;
import at.sparklingscience.urbantrees.domain.SearchResult;
import at.sparklingscience.urbantrees.domain.User;
import at.sparklingscience.urbantrees.domain.UserBulkAction;
import at.sparklingscience.urbantrees.domain.UserCreation;
import at.sparklingscience.urbantrees.domain.UserLight;
import at.sparklingscience.urbantrees.domain.UserPermission;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.DuplicateUsernameException;
import at.sparklingscience.urbantrees.exception.UnauthorizedException;
import at.sparklingscience.urbantrees.mapper.AuthMapper;
import at.sparklingscience.urbantrees.security.AuthSettings;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;
import at.sparklingscience.urbantrees.security.authentication.jwt.JWTUserAuthentication;
import at.sparklingscience.urbantrees.security.authentication.otp.OtpValidationException;
import at.sparklingscience.urbantrees.security.authentication.otp.Totp;
import io.jsonwebtoken.io.Decoders;
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
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

	/**
	 * NOTE:
	 * Something is fucked up on PROD and it thinks there's a circular dep?!
	 * This does not happen locally though, so due to time-restraints this
	 * workaround is used.
	 */
	//@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
	
	@Autowired
	private AuthMapper authMapper;
	
	@Autowired
	private UserService userService;
	
	@Value("${at.sparklingscience.urbantrees.dateFormatPattern}")
	private String dateFormatPattern;
	
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
		this.authMapper.deleteAllUserSessions(userId);
		
	}
	
	/**
	 * Inserts a new user into the database.
	 * @param username Username to register (raw).
	 * @param rawPassword Password entered by the user (raw).
	 * @param roles Roles to assign to the new user.
	 * @return The given user object with ID set.
	 * @throws DuplicateUsernameException if any of the given usernames already existed.
	 * 								 	  This rolls back any changes that may have occurred
	 * 								 	  before that.
	 */
	@Transactional
	public List<UserLight> registerUsers(final UserCreation creation)
			throws DuplicateUsernameException {
		
		List<UserLight> registeredUsers = new ArrayList<UserLight>();
		
		User template = creation.getTemplate();
		for (String username : creation.getUsernames()) {
			registeredUsers.add(this.registerUser(username, null, template.getRoles()));
		}
		
		return registeredUsers;
		
	}
	
	/**
	 * Inserts a new user into the database.
	 * If the given password is null, we automatically generate a secure login key.
	 * Note: Transactional annotation of this method has been removed
	 * 		 since this method is called in {@link #registerUsers(String, String, List)}.
	 * @param username Username to register (raw).
	 * @param rawPassword Password entered by the user (raw).
	 * @param roles Roles to assign to the new user.
	 * @return the newly created user.
	 * @throws DuplicateUsernameException if the username was already given to another user.
	 */
	public UserLight registerUser(final String username, final String rawPassword, final List<Role> roles)
			throws DuplicateUsernameException {
		
		User newUser = new User();
		newUser.setUsername(username);
		
		boolean generateLoginKey = false;
		if (rawPassword == null) {
			newUser.setPassword(null);
			generateLoginKey = true;
		} else {
			newUser.setPassword(this.bCryptPasswordEncoder.encode(rawPassword));
		}
		
		try {
			this.authMapper.insertUser(newUser);			
		} catch (DuplicateKeyException e) {
			throw new DuplicateUsernameException(e, username);
		}
		if (roles != null && roles.size() > 0) {
			this.authMapper.insertUserRoles(newUser.getId(), roles);
		}
		this.userService.prepareXp(newUser.getId());
		if (generateLoginKey) {
			this.getLoginKey(newUser.getId());			
		}
		
		return this.authMapper.findUserLightById(newUser.getId());
		
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
	public void increaseFailedLoginAttempts(final int userId) {
		this.authMapper.increaseFailedLoginAttempts(userId);
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
	 * Whether the receivingUser has the given permission from the grantingUser.
	 * If grantingUserId equals receivingUserId, this method always returns true.
	 * @param grantingUserId User ID of permission granting user (giving permission)
	 * @param receivingUser Permission receiving user (getting permission)
	 * @param permission type of permission
	 * @see #hasUserPermission(int[], int, UserPermission)
	 */
	@Transactional
	public boolean hasUserPermission(int grantingUserId, AuthenticationToken receivingUser, UserPermission permission) {
		return this.hasUserPermission(grantingUserId, receivingUser.getId(), permission);
	}

	/**
	 * Whether the receivingUser has the given permission from the grantingUser.
	 * If grantingUserId equals receivingUserId, this method always returns true.
	 * @param grantingUserId User ID of permission granting user (giving permission)
	 * @param receivingUserId User ID of permission receiving user (getting permission)
	 * @param permission type of permission
	 * @see #hasUserPermission(int[], int, UserPermission)
	 */
	@Transactional
	public boolean hasUserPermission(int grantingUserId, int receivingUserId, UserPermission permission) {
		if (grantingUserId == receivingUserId) {
			return true;
		}
		return this.hasUserPermission(new int[] {grantingUserId}, receivingUserId, permission);
	}

	/**
	 * Whether the receivingUser has the given permission from all grantingUsers.
	 * If grantingUserIds contains receivingUserId, it is removed from the array.
	 * If grantingUserIds only contains receivingUserId, this method always returns true.
	 * @param grantingUserId User ID of permission granting user (giving permission)
	 * @param receivingUserId User ID of permission receiving user (getting permission)
	 * @param permission type of permission
	 */
	@Transactional
	public boolean hasUserPermission(int[] grantingUserIds, int receivingUserId, UserPermission permission) {
		final int[] grantingUserIdsWOReceivingUser = Arrays.stream(grantingUserIds)
				.filter((id) -> id != receivingUserId)
				.toArray();
		if (grantingUserIdsWOReceivingUser.length == 0) {
			return true;
		}
		return this.authMapper.hasUserPermission(grantingUserIdsWOReceivingUser, receivingUserId, permission.name());
	}
	
	/**
	 * Fetches all users in a light version that match the given filters.
	 * Important Note: THIS METHOD MUST SOLELY BE CALLED BY AN ADMIN VIA ADMINCONTROLLER!
	 * @param filters filters to restrict users to return
	 * @param limit limit nr. of results
	 * @param offset page offset
	 * @return list of users wrapped in a {@link SearchResult} met-info wrapper
	 * @throws BadRequestException if conversion of filter strings to date fails
	 */
	public SearchResult<List<UserLight>> getUsersLight(Map<String, Object> filters,
													   Integer limit,
													   Integer offset) throws BadRequestException {

		this.prepareUserSearchFilters(filters);
		
		List<UserLight> results = this.authMapper.findUsersLight(filters, limit, offset);
		results.forEach(r -> r.setNonLocked(this.isUserNonLocked(r)));
		
		int totalResultAmount = this.authMapper.findUsersLightAmount(filters);
		return new SearchResult<List<UserLight>>(results).withMetadata("totalResultAmount", totalResultAmount);
		
	}
	
	/**
	 * Prepare search filters for user search.
	 * This converts predefined fields from string to date.
	 * @param filters given filters from teh frontend
	 */
	private void prepareUserSearchFilters(final Map<String, Object> filters) {
		
		try {
			ControllerUtil.filterStringToDate(
					this.dateFormatPattern,
					filters,
					"lastLoginDateFrom",
					"lastLoginDateTo",
					"creationDateFrom",
					"creationDateTo"
					);
		} catch (ParseException e) {
			LOGGER.warn("Illegal date format for filters: " + e.getMessage(), e);
			throw new BadRequestException("Illegal date format for filters.");
		}
		
	}
	
	/**
	 * Execute the given bulk action for all users matching the given filters.
	 * @param filters filters the users must match
	 * @param action action to execute for every matching user
	 * @return all {@link UserLight}s that were affected in the state they were in *before* the bulk action
	 */
	@Transactional
	public List<UserLight> executeBulkAction(Map<String, Object> filters,
							 				 UserBulkAction action) {
		
		this.prepareUserSearchFilters(filters);
		
		List<UserLight> users = this.authMapper.findUsersLight(filters, null, null);
		users.forEach(u -> {
			try {
				switch (action) {
				case EXPIRE_CREDENTIALS:
					this.expireCredentials(u.getId());
					break;
	
				case CREATE_LOGIN_LINKS:
					this.getLoginKey(u.getId());
					break;
					
				case ACTIVATE:
					this.activate(u.getId());
					break;
					
				case INACTIVATE:
					this.inactivate(u.getId());
					break;
					
				case DELETE:
					this.deleteUser(u.getId());
					break;
	
				default:
					throw new IllegalArgumentException("Unsupported user bulk action: " + action);
				}
			} catch (Throwable t) {
				throw new RuntimeException("Failed to execute bulk action " + action + " on user " + u.getId(), t);
			}
		});
		
		return users;
		
	}
	
	/**
	 * Fetches all user roles available.
	 */
	public List<Role> getAllUserRoles() {
		
		return this.authMapper.findAllUserRoles();
		
	}
	
	/**
	 * Fetches all roles currently assigned to the given user.
	 */
	public List<Role> getUserRoles(int userId) {
		
		return this.authMapper.findRolesByUserId(userId);
		
	}
	
	/**
	 * Get the current JWT secret key for the given user.
	 * @param userId user's id
	 * @param authId id of the current session
	 * @return {@link SecretKey}
	 * @throws WeakKeyException if the stored user secret is too weak for the used algorithm.
	 * @throws UnauthorizedException if the given user is not found, no secret is found, or the user is inactivated.
	 */
	public SecretKey getJWTSecret(final int userId, final long authId) throws WeakKeyException, UnauthorizedException {
		
		final String base64Secret = this.authMapper.findUserTokenSecret(userId, authId);
		if (base64Secret == null) {
			throw new UnauthorizedException("Could not find user's login token. (user id = " + userId + ")", null);
		}
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
		
	}
	
	/**
	 * Generate a new JWT secret for the given user and store it.
	 * @param user user to update (from the security package)
	 * @return the stored auth session if it was successfully inserted or updated
	 * @throws RuntimeException if storing the secret could not be stored.
	 */
	public JWTUserAuthentication newSession(at.sparklingscience.urbantrees.security.user.User user) {
		
		final SecretKey signingKey = Keys.secretKeyFor(SecurityConfiguration.JWT_AUTHENTICATION_SIG_ALG);
		final JWTUserAuthentication auth = new JWTUserAuthentication(user.getId(), signingKey);
		
		final int updatedUsers = this.authMapper.upsertUserAuthentication(auth);
		if (updatedUsers != 1) {
			throw new RuntimeException("Token secret could not be stored for user " + user.getId() + ", instead " + updatedUsers + " users have been updated.");
		}
		
		return auth;
		
	}
	
	@Transactional
	public void validateOtp(final int userId, final String inputCode) throws OtpValidationException {
		
		LOGGER.trace("Validating OTP for user {}...", userId);
		OtpCredentials otpCreds = this.authMapper.findUserOtpCredentials(userId);
		
		try {
			Totp totp = new Totp(otpCreds.getSecret(), otpCreds.getScratchCodes())
					.verify(inputCode);
			this.authMapper.updateUserOtpCredentials(userId, otpCreds.scratchCodes(totp.scratchCodes()));
		} catch (OtpValidationException e) {
			LOGGER.warn("OTP validation failed for user {}", userId, e);
			throw e;
		}
		
		LOGGER.trace("Successfully validated OTP for user {}.", userId);
		
	}
	
}
