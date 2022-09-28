package at.sparklingscience.urbantrees.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

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
import at.sparklingscience.urbantrees.domain.Role;
import at.sparklingscience.urbantrees.domain.SearchResult;
import at.sparklingscience.urbantrees.domain.User;
import at.sparklingscience.urbantrees.domain.UserBulkAction;
import at.sparklingscience.urbantrees.domain.UserBulkActionData;
import at.sparklingscience.urbantrees.domain.UserCreation;
import at.sparklingscience.urbantrees.domain.UserLevelAction;
import at.sparklingscience.urbantrees.domain.UserLight;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.DuplicateUsernameException;
import at.sparklingscience.urbantrees.exception.InternalException;
import at.sparklingscience.urbantrees.mapper.AuthMapper;
import at.sparklingscience.urbantrees.mapper.UserMapper;
import at.sparklingscience.urbantrees.util.QrCodeRenderer;
import io.nayuki.qrcodegen.QrCode;

/**
 * Service for user admin-related actions.
 * 
 * @author Laurenz Fiala
 * @since 2022/09/13
 */
@Service
public class AdminService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminService.class);
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private ApplicationService appService;
	
	@Autowired
	private AuthenticationService authService;
	
	@Autowired
	private AuthMapper authMapper;
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private Font loginQrFont;
	
	@Value("${at.sparklingscience.urbantrees.dateFormatPattern}")
	private String dateFormatPattern;
	
	@Value("${at.sparklingscience.urbantrees.loginQrUri}")
	private String loginQrUri;
	
	/**
	 * @see AuthenticationService#findUser(int)
	 */
	public User findUser(final int userId) {
		return this.authService.findUser(userId);
	}
	
	public List<UserLight> findUsersLightById(final List<Integer> userIds) {
		return this.authMapper.findUsersLightById(userIds);
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
	public UserLight registerUser(final String username,
								  final String rawPassword,
								  final List<Role> roles)
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
		// prepare xp
		try {
			this.userMapper.insertLevel(
					newUser.getId(),
					UserLevelAction.INITIAL.getDefaultRewardXp(),
					UserLevelAction.INITIAL.toString(),
					null
			);			
		} catch (Throwable t) {
			LOGGER.error("Failed to insert XP for user {}: {}", newUser.getId(), t.getMessage(), t);
			this.appService.logExceptionEvent("Failed to insert XP for user " + newUser.getId() + ": " + t.getMessage(), t);
		}
		if (generateLoginKey) {
			this.getLoginKey(newUser.getId());			
		}
		
		return this.authMapper.findUserLightById(newUser.getId());
		
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
	
	@Transactional
	public void modifyRoles(int userId, List<Role> roles, boolean isAdd) {
		
		if (isAdd) {
			this.authMapper.insertUserRoles(userId, roles);
		} else {
			this.authMapper.deleteUserRoles(userId, roles);
		}
		
	}
	
	/**
	 * TODO
	 * @param userId
	 * @return
	 */
	public String getLoginKey(int userId) {
	    return this.getLoginKey(userId, new Date(System.currentTimeMillis() + SecurityConfiguration.LOGIN_LINK_EXPIRATION_TIME));
	}
	
	@Transactional
	public String getLoginKey(int userId, Date expirationDate) {
		
		String storedToken = this.authMapper.findUserLoginKey(userId);
		if (storedToken != null) {
			return storedToken;
		}
		
		final String secureToken = this.generateSecureToken();
	    this.authMapper.updateUserLoginKey(
    		userId,
    		secureToken,
    		expirationDate
		);
	    
	    return secureToken;
		
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
	
	public void deleteUser(final int userId) {
		
		this.authService.deleteUser(userId);
		
	}
	
	/**
	 * Expire the given users' credentials.
	 */
	@Transactional
	public void expireCredentials(final int userId) {
		
		this.authMapper.updateCredentialsNonExpired(userId, false);
		
	}
	
	/**
	 * Un-expire the given users' credentials.
	 */
	@Transactional
	public void unexpireCredentials(final int userId) {
		
		this.authMapper.updateCredentialsNonExpired(userId, true);
		
	}
	
	/**
	 * Update the expiration date of the current login key for the given user.
	 * @param user user to be affected
	 * @param expiration time until the login key is valid, or null to be valid indefinitely
	 * @throws RuntimeException if no login key is currently set
	 */
	public void updateLoginKeyExpirationDate(User user, Date expiration) {
		if (user.getSecureLoginKey() == null) {
			throw new RuntimeException("User has no login key set.");
		}
		this.authMapper.updateUserLoginKey(user.getId(), user.getSecureLoginKey(), expiration);
	}
	
	/**
	 * Generate a QR code that lets the user login in and
	 * set a PIN.
	 * @param user user to generate QR code for
	 * @return qr code
	 */
	public QrCode generateLoginQr(User user) {
		
		final String loginKey = user.getSecureLoginKey();
		String uri = this.loginQrUri;
		uri = uri.replace("{token}", loginKey);
		return QrCode.encodeText(uri, QrCode.Ecc.HIGH);
		
	}
	
	/**
	 * TODO
	 * @param userId
	 * @param outputStream
	 */
	public void writeLoginQr(final User user,
							 OutputStream outputStream) {
		
		if (user == null) {
			throw new BadRequestException("User does not exist");
		}
		this.updateLoginKeyExpirationDate(user, null);
		this.unexpireCredentials(user.getId());
		var qrCode = this.generateLoginQr(user);
		
		var bufferedImage = QrCodeRenderer.toImage(qrCode, 5, 8);
		Graphics2D imageGraphics = (Graphics2D) bufferedImage.getGraphics();
	    
	    imageGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

	    imageGraphics.setFont(this.loginQrFont);
	    imageGraphics.setColor(Color.black);
	    imageGraphics.drawString(user.getUsername(), 40, this.loginQrFont.getSize());
	    
		try {
			ImageIO.write(bufferedImage, "png", outputStream);
		} catch (IOException e) {
			throw new InternalException("Failed to generate login QR", e);
		}
		
	}
	
	/**
	 * TODO
	 * @param affectedUsers
	 * @param outputStream
	 */
	public void writeBulkLoginQr(final List<UserLight> affectedUsers,
								 OutputStream outputStream) {
		
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {

			for (var user : affectedUsers) {
				
				if (user.getSecureLoginKey() == null) {
					user.setSecureLoginKey(this.getLoginKey(user.getId(), null));
				} else {
					this.updateLoginKeyExpirationDate(user, null);									
				}
				
				var zipEntry = new ZipEntry("login_qr_" + user.getUsername() + ".png");
				zipOutputStream.putNextEntry(zipEntry);
				this.writeLoginQr(user, zipOutputStream);
				zipOutputStream.closeEntry();

			}
			
		} catch (Exception e) {
			LOGGER.error("Failed to generate login QR-zip", e);
			throw new InternalException("Failed to generate downloadable resource");
		}
		
		
	}
	
	private String generateSecureToken() {
		
		Random random = new SecureRandom();
		byte[] randomData = new byte[SecurityConfiguration.SECURE_LOGIN_KEY_BYTES];
		random.nextBytes(randomData);
		
		Encoder encoder = Base64.getUrlEncoder().withoutPadding();
	    return encoder.encodeToString(randomData);
		
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
		
		List<UserLight> allResults = this.authMapper.findUsersLight(filters, null, null);
		Integer startIndex = 0;
		if (offset != null) {
			startIndex = Math.max(Math.min(offset, allResults.size()), 0);
		}
		Integer endIndex = allResults.size();
		if (limit != null) {
			endIndex = Math.max(Math.min(limit, allResults.size()), 0);
		}
		
		List<UserLight> results = allResults.subList(offset, endIndex);
		results.forEach(r -> r.setNonLocked(this.authService.isUserNonLocked(r)));
		
		int totalResultAmount = this.authMapper.findUsersLightAmount(filters);
		return new SearchResult<List<UserLight>>(results)
				.withMetadata("totalResultAmount", totalResultAmount)
				.withTid(this.appService.transaction(
							allResults.stream().map(u -> u.getId()).toArray()
						));
		
	}
	
	/**
	 * TODO
	 * @param filters
	 * @return
	 * @throws BadRequestException
	 */
	public SearchResult<List<UserLight>> getUserSearchTransaction(Map<String, Object> filters) throws BadRequestException {

		List<UserLight> results = this.authMapper.findUsersLight(filters, null, null);
		
		return new SearchResult<List<UserLight>>(results)
				.withMetadata("totalResultAmount", results.size())
				.withTid(this.appService.transaction(
							results.stream().map(u -> u.getId()).toArray()
						));
		
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
	 * Fetches all user roles available.
	 */
	public List<Role> getAllUserRoles() {
		
		return this.authMapper.findAllUserRoles();
		
	}
	
	/**
	 * Execute the given bulk action for all users matching the given filters.
	 * @param action action to execute for every matching user
	 * @param data filters & additional data that may be needed for certain actions
	 * @return all {@link UserLight}s that were affected in the state they were in *before* the bulk action
	 */
	@Transactional
	public List<UserLight> executeBulkAction(UserBulkAction action,
											 UUID transactionId, 
							 				 UserBulkActionData data) {
		
		List<Integer> userIds = this.appService.getTransaction(transactionId);
		List<UserLight> affectedUsers = this.findUsersLightById(userIds);
		
		affectedUsers.forEach(u -> {
			try {
				switch (action) {
				case EXPIRE_CREDENTIALS:
					this.expireCredentials(u.getId());
					break;
	
				case CREATE_LOGIN_LINKS:
					this.getLoginKey(u.getId());
					break;
	
				case CREATE_LOGIN_LINKS_PERMANENT:
					this.getLoginKey(u.getId(), null);
					break;
	
				case ADD_ROLES:
					this.modifyRoles(u.getId(), data.getRoles(), true);
					break;
	
				case REMOVE_ROLES:
					this.modifyRoles(u.getId(), data.getRoles(), false);
					break;
					
				case ACTIVATE:
					this.activate(u.getId());
					break;
					
				case INACTIVATE:
					this.inactivate(u.getId());
					break;
					
				case DELETE:
					this.authService.deleteUser(u.getId());
					break;
	
				default:
					throw new IllegalArgumentException("Unsupported user bulk action: " + action);
				}
			} catch (Throwable t) {
				throw new RuntimeException("Failed to execute bulk action " + action + " on user " + u.getId(), t);
			}
		});
		
		return affectedUsers;
		
	}

}
