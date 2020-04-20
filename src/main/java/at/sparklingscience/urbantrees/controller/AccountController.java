package at.sparklingscience.urbantrees.controller;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import at.sparklingscience.urbantrees.controller.util.ControllerUtil;
import at.sparklingscience.urbantrees.domain.PasswordReset;
import at.sparklingscience.urbantrees.domain.UserIdentity;
import at.sparklingscience.urbantrees.domain.UserPermission;
import at.sparklingscience.urbantrees.domain.UserPermissionRequest;
import at.sparklingscience.urbantrees.domain.UsernameChange;
import at.sparklingscience.urbantrees.security.jwt.AuthenticationToken;
import at.sparklingscience.urbantrees.service.AccountService;

/**
 * Handles actions on user accounts.
 * 
 * @author Laurenz Fiala
 * @since 2018/07/13
 */
@RestController
@RequestMapping("/account")
public class AccountController {
	
	private static Logger logger;
	
	@Autowired
	private AccountService accountService;
	
	public AccountController(Logger classLogger) {
		logger = classLogger;
	}
	
	@RequestMapping(method = RequestMethod.PUT, path = "/changepassword")
	public void putChangePassword(@RequestBody PasswordReset passwordReset, Authentication auth) {
		
		final AuthenticationToken authToken = ControllerUtil.getAuthToken(auth);
		
		logger.debug("[[ PUT ]] putChangePassword - reset password for user: {}", authToken.getDetails());
		
		this.accountService.changePassword(authToken.getId(), authToken.getAuthorities(), passwordReset);
		
		logger.debug("[[ PUT ]] putChangePassword |END| Successfully changed user password.");
		
	}

	@RequestMapping(method = RequestMethod.PUT, path = "/changeusername")
	public void putChangeUsername(@RequestBody UsernameChange usernameChange, Authentication auth) {
		
		final AuthenticationToken authToken = ControllerUtil.getAuthToken(auth);
		
		logger.debug("[[ PUT ]] putChangeUsername - change username for user: {} to: {}", authToken.getDetails(), usernameChange.getUsername());
		
		this.accountService.changeUsername(authToken.getId(), usernameChange);
		
		logger.debug("[[ PUT ]] putChangeUsername |END| Successfully changed username.");
		
	}

	@RequestMapping(method = RequestMethod.POST, path = "/permission/request")
	public UserIdentity postUserPermissionRequest(@RequestBody UserPermissionRequest permRequest, Authentication auth) {
		
		final AuthenticationToken authToken = ControllerUtil.getAuthToken(auth);

		logger.debug(
			"[[ POST ]] postUserPermissionRequest - add permission: {} from user: {} to user: {}",
			permRequest.getPermission(),
			permRequest.getUsername(),
			authToken.getUsername()
		);

		UserIdentity ui = this.accountService.requestUserPermission(permRequest, authToken.getId());
		
		logger.debug(
			"[[ POST ]] postUserPermissionRequest |END| Successfully added permission: {} from user: {} to user: {}",
			permRequest.getPermission(),
			permRequest.getUsername(),
			authToken.getUsername()
		);
		
		return ui;
		
	}

	@RequestMapping(method = RequestMethod.GET, path = "/permission/granted/{permission}")
	public List<UserIdentity> getUsersGrantingPermission(@PathVariable UserPermission permission, Authentication auth) {
		
		final int receivingUserId = ControllerUtil.getAuthToken(auth).getId();

		logger.debug(
			"[[ GET ]] getUsersGrantingPermission - get granting users for receiving user: {} with perm: {}",
			receivingUserId,
			permission
		);

		List<UserIdentity> grantingUsers = this.accountService.getUsersGrantingPermission(receivingUserId, permission);
		
		logger.debug(
				"[[ GET ]] getUsersGrantingPermission |END| Successfully got granting users for receiving user: {} with perm: {}",
				receivingUserId,
				permission
			);
		
		return grantingUsers;
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/permission/pin")
	public String getPermissionsPIN(Authentication auth) {
		
		final int userId = ControllerUtil.getAuthToken(auth).getId();
		logger.debug("[[ GET ]] getPermissionsPIN - generate new PPIN for user: {}", userId);
		
		return this.accountService.newPermissionsPIN(userId);
		
	}

}
