package at.sparklingscience.urbantrees.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import at.sparklingscience.urbantrees.SecurityConfiguration;
import at.sparklingscience.urbantrees.controller.util.ControllerUtil;
import at.sparklingscience.urbantrees.domain.PasswordReset;
import at.sparklingscience.urbantrees.domain.User;
import at.sparklingscience.urbantrees.domain.UserIdentity;
import at.sparklingscience.urbantrees.domain.UserPermission;
import at.sparklingscience.urbantrees.domain.UserPermissionRequest;
import at.sparklingscience.urbantrees.domain.UsernameChange;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.ClientError;
import at.sparklingscience.urbantrees.exception.UnauthorizedException;
import at.sparklingscience.urbantrees.security.SecurityUtil;
import at.sparklingscience.urbantrees.security.jwt.AuthenticationToken;
import at.sparklingscience.urbantrees.security.user.AuthenticationService;

/**
 * Handles actions on user accounts.
 * 
 * @author Laurenz Fiala
 * @since 2018/07/13
 */
@RestController
@RequestMapping("/account")
public class AccountController {
	
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);
	
	@Autowired
	private AuthenticationService authenticationService;
	
	@RequestMapping(method = RequestMethod.PUT, path = "/changepassword")
	public void putChangePassword(@RequestBody PasswordReset passwordReset, Authentication auth) {
		
		final AuthenticationToken authToken = ControllerUtil.getAuthToken(auth);
		
		LOGGER.debug("[[ PUT ]] putChangePassword - reset password for user: {}", authToken.getDetails());
		
		boolean changeWithoutOldPw = false;
		if (auth.getAuthorities().contains(SecurityUtil.grantedAuthority(SecurityConfiguration.TEMPORARY_CHANGE_PASSWORD_ACCESS_ROLE))) {
			changeWithoutOldPw = true;
		}
		boolean pwChanged = this.authenticationService.changePassword(authToken.getId(), passwordReset.getOldPassword(), passwordReset.getNewPassword(), changeWithoutOldPw);
		
		if (!pwChanged) {
			throw new BadRequestException("Old password is incorrect.");
		}
		
		LOGGER.debug("[[ PUT ]] putChangePassword |END| Successfully changed user password.");
		
	}

	@RequestMapping(method = RequestMethod.PUT, path = "/changeusername")
	public void putChangeUsername(@RequestBody UsernameChange payload, Authentication auth) {
		
		final AuthenticationToken authToken = ControllerUtil.getAuthToken(auth);
		final String newUsername = payload.getUsername();
		
		LOGGER.debug("[[ PUT ]] putChangeUsername - change username for user: {} to: {}", authToken.getDetails(), newUsername);
		
		this.authenticationService.changeUsername(authToken.getId(), newUsername);
		
		LOGGER.debug("[[ PUT ]] putChangeUsername |END| Successfully changed username.");
		
	}

	@RequestMapping(method = RequestMethod.POST, path = "/permission/request")
	public UserIdentity postUserPermissionRequest(@RequestBody UserPermissionRequest payload, Authentication auth) {
		
		final AuthenticationToken authToken = ControllerUtil.getAuthToken(auth);

		LOGGER.debug(
			"[[ POST ]] postUserPermissionRequest - add permission: {} from user: {} to user: {}",
			payload.getPermission(),
			payload.getUsername(),
			authToken.getUsername()
		);

		final User user = this.authenticationService.findUser(payload.getUsername());
		if (user == null) {
			throw new UnauthorizedException("Permission Request: Username or password wrong");
		}
		
		if (authToken.getId() == user.getId()) {
			throw new BadRequestException("Permission Request: User may not request permission from themself", ClientError.SAME_USER_PERM_REQUEST);
		}
		
		final boolean authValid = this.authenticationService.isPermissionPINValid(user.getId(), payload.getPpin());
		final boolean userOk = this.authenticationService.isUserNonLocked(user);
		
		if (!authValid) {
			try {
				this.authenticationService.increaseFailedLoginAttempts(payload.getUsername());
			} catch (Throwable t) {}
			throw new UnauthorizedException("Permission Request: Username or password wrong");
		}
		
		if (!userOk) {
			throw new UnauthorizedException("Permission Request: Username or password wrong");
		}
		
		this.authenticationService.successfulAuth(user.getId());
		this.authenticationService.addUserPermission(user.getId(), authToken.getId(), payload.getPermission());
		
		LOGGER.debug(
				"[[ POST ]] postUserPermissionRequest |END| Successfully added permission: {} from user: {} to user: {}",
				payload.getPermission(),
				payload.getUsername(),
				authToken.getUsername()
			);
		
		return UserIdentity.fromUser(user);
		
	}

	@RequestMapping(method = RequestMethod.GET, path = "/permission/granted/{permission}")
	public List<UserIdentity> getUsersGrantingPermission(@PathVariable UserPermission permission, Authentication auth) {
		
		final int receivingUserId = ControllerUtil.getAuthToken(auth).getId();

		LOGGER.debug(
			"[[ GET ]] getUsersGrantingPermission - get granting users for receiving user: {} with perm: {}",
			receivingUserId,
			permission
		);

		List<UserIdentity> grantingUsers = this.authenticationService.getUsersGrantingPermission(receivingUserId, permission);
		
		LOGGER.debug(
				"[[ GET ]] getUsersGrantingPermission |END| Successfully got granting users for receiving user: {} with perm: {}",
				receivingUserId,
				permission
			);
		
		return grantingUsers;
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/permission/pin")
	public String getPermissionsPIN(Authentication auth) {
		
		final int userId = ControllerUtil.getAuthToken(auth).getId();
		LOGGER.debug("[[ GET ]] getPermissionsPIN - generate new PPIN for user: {}", userId);
		
		return this.authenticationService.newPermissionsPIN(userId);
		
	}

}
