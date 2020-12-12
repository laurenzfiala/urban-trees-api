package at.sparklingscience.urbantrees.service;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import at.sparklingscience.urbantrees.SecurityConfiguration;
import at.sparklingscience.urbantrees.domain.OtpCredentials;
import at.sparklingscience.urbantrees.domain.PasswordReset;
import at.sparklingscience.urbantrees.domain.User;
import at.sparklingscience.urbantrees.domain.UserIdentity;
import at.sparklingscience.urbantrees.domain.UserPermission;
import at.sparklingscience.urbantrees.domain.UserPermissionRequest;
import at.sparklingscience.urbantrees.domain.UsernameChange;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.ClientError;
import at.sparklingscience.urbantrees.exception.InternalException;
import at.sparklingscience.urbantrees.exception.UnauthorizedException;
import at.sparklingscience.urbantrees.mapper.AuthMapper;
import at.sparklingscience.urbantrees.security.SecurityUtil;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;
import at.sparklingscience.urbantrees.security.authentication.otp.OtpValidationException;
import at.sparklingscience.urbantrees.security.authentication.otp.Totp;

/**
 * Service for user account-related actions.
 * 
 * @author Laurenz Fiala
 * @since 2020/04/19
 */
@Service
public class AccountService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private AuthenticationService authService;
	
	@Autowired
	private AuthMapper authMapper;
	
	/**
	 * Change the passwor dof the given user.
	 * If the user has the temporary change password role assigned, they may change their
	 * password without the old password.
	 * @param userId user to change password of
	 * @param authorities granted roles of the given user
	 * @param passwordReset password reset dto (holds old and new password)
	 */
	public void changePassword(final int userId, final Collection<? extends GrantedAuthority> authorities, final PasswordReset passwordReset) {
		
		boolean changeWithoutOldPw = false;
		if (authorities.contains(SecurityUtil.grantedAuthority(SecurityConfiguration.TEMPORARY_CHANGE_PASSWORD_ACCESS_ROLE))) {
			changeWithoutOldPw = true;
		}
		boolean pwChanged = this.changePassword(userId, passwordReset.getOldPassword(), passwordReset.getNewPassword(), changeWithoutOldPw);
		
		if (!pwChanged) {
			throw new BadRequestException("Old password is incorrect or invalid user id was given.");
		}
		
		LOGGER.trace("Successfully changed password for user " + userId);
		
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
	private boolean changePassword(final int userId, final String oldPassword, final String newPassword, final boolean changeWithoutOldPw) {
		
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
	 * Update a users' username to {@link UsernameChange#getUsername()}.
	 * @param userId the user to be affected
	 * @param usernameChange the new username
	 */
	@Transactional
	public void changeUsername(final int userId, final UsernameChange usernameChange) {
		
		LOGGER.debug("Upding username for user " + userId + "...");
		
		final String newUsername = usernameChange.getUsername();
		if (newUsername == null || newUsername.isBlank() || newUsername.length() < 5) {
			throw new BadRequestException("Username does not meet requirements: length >= 5"); 
		}
		
		int updatedRows;
		try {
			updatedRows = this.authMapper.updateUsername(userId, newUsername);
		} catch (Throwable t) {
			throw new BadRequestException("Duplicate username.", ClientError.USERNAME_DUPLICATE);
		}
		
		if (updatedRows == 0) {
			LOGGER.info("Failed to update usename for unknown reason. Please investigate.");
			throw new InternalException("Failed to update username.");
		}
		LOGGER.debug("Username for " + userId + " successfully updated.");
		
	}
	
	/**
	 * Add a user-permission to toUserId. If the credentials of the user in permRequest is valid,
	 * this user grants the requested permission to toUserId.
	 * @param permRequest user to grant permission and type of permission
	 * @param toUser user to receive permission
	 * @return the granting users' identity, which is now granted to be shown to user with toUserId.
	 */
	public UserIdentity requestUserPermission(final UserPermissionRequest permRequest, final AuthenticationToken toUser) {
		
		final User fromUser = this.authService.findUser(permRequest.getUsername());
		if (fromUser == null) {
			throw new UnauthorizedException("Permission Request: Username or password wrong", toUser);
		}
		
		if (toUser.getId() == fromUser.getId()) {
			throw new BadRequestException("Permission Request: User may not request permission from themself", ClientError.SAME_USER_PERM_REQUEST);
		}
		
		final boolean fromUserAuthValid = this.authService.isPermissionPINValid(fromUser.getId(), permRequest.getPpin());
		final boolean fromUserOk = this.authService.isUserNonLocked(fromUser);
		
		if (!fromUserAuthValid) {
			try {
				this.authService.increaseFailedLoginAttempts(fromUser.getId());
			} catch (Throwable t) {}
			throw new UnauthorizedException("Permission Request: Username or password wrong", toUser);
		}
		
		if (!fromUserOk) {
			throw new UnauthorizedException("Permission Request: Username or password wrong", toUser);
		}
		
		this.authService.successfulAuth(fromUser.getId());
		this.authService.addUserPermission(fromUser.getId(), toUser.getId(), permRequest.getPermission());
		
		return UserIdentity.fromUser(fromUser);
		
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
	
	@Transactional
	public boolean isUsingOtp(final int userId) {
		
		return this.authMapper.findUserById(userId).isUsingOtp();
		
	}
	
	@Transactional
	public String initNewOtp(final int userId) throws AccessDeniedException {
		
		LOGGER.trace("Initializing new OTP for user {}...", userId);
		
		if (this.authMapper.isUserUsingOtp(userId)) {
			throw new AccessDeniedException("User may not request new OTP when one is already active. user: " + userId);
		}
		
		Totp totp = new Totp();
		this.authMapper.updateUserOtpCredentials(userId, new OtpCredentials(totp.secret(), totp.scratchCodes()));
		
		LOGGER.trace("Successfully generated new OTP credentials for user {}...", userId);
		
		return totp.secret();
		
	}
	
	@Transactional
	public String[] validateNewOtp(final int userId, final String inputCode) throws OtpValidationException {
		
		LOGGER.trace("Validating new OTP for user {}...", userId);
		OtpCredentials otpCreds = this.authMapper.findUserOtpCredentials(userId);
		
		if (this.authMapper.isUserUsingOtp(userId)) {
			LOGGER.warn("User requested to activate OTP when it was already active. user: {}", userId);
			throw new OtpValidationException("You may not activate OTP if it is already activated.");
		}
		
		try {
			new Totp(otpCreds.getSecret(), otpCreds.getScratchCodes())
				.verifyTotpOnly(inputCode);
		} catch (OtpValidationException e) {
			LOGGER.warn("Failed to validate OTP for user {}.", userId, e);
			this.authMapper.updateUserUsingOtp(userId, false);
			//this.authMapper.increaseFailedLoginAttempts(userId); user is assumed to be trusted here, so don't increase
			throw e;
		}
		
		this.authMapper.updateUserUsingOtp(userId, true);
		LOGGER.trace("Successfully validated new OTP for user {} and activated OTP.", userId);
		
		return otpCreds.getScratchCodes();
		
	}
	
	@Transactional
	public void deactivateOtp(final int userId, final String inputCode) throws OtpValidationException {
		
		LOGGER.trace("Deactivating OTP for user {}...", userId);
		OtpCredentials otpCreds = this.authMapper.findUserOtpCredentials(userId);
		
		new Totp(otpCreds.getSecret(), otpCreds.getScratchCodes())
			.verify(inputCode);
		
		this.authMapper.updateUserUsingOtp(userId, false);
		LOGGER.trace("Successfully deactivated OTP for user {}.", userId);
		
	}

}
