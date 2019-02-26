package at.sparklingscience.urbantrees.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import at.sparklingscience.urbantrees.SecurityConfiguration;
import at.sparklingscience.urbantrees.controller.util.ControllerUtil;
import at.sparklingscience.urbantrees.domain.PasswordReset;
import at.sparklingscience.urbantrees.domain.UsernameChange;
import at.sparklingscience.urbantrees.exception.BadRequestException;
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

}
