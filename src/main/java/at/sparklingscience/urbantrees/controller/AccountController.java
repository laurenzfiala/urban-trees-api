package at.sparklingscience.urbantrees.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import at.sparklingscience.urbantrees.domain.PasswordReset;
import at.sparklingscience.urbantrees.exception.BadRequestException;
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
	public void putChangePassword(@RequestBody PasswordReset passwordReset, Principal principal) {
		
		final String userId = principal.getName();
		
		LOGGER.debug("[[ PUT ]] putChangePassword - reset password for user: {}", userId);
		
		boolean pwChanged = this.authenticationService.changePassword(userId, passwordReset.getOldPassword(), passwordReset.getNewPassword());
		
		if (!pwChanged) {
			throw new BadRequestException("Old password is incorrect.");
		}
		
		LOGGER.debug("[[ PUT ]] putChangePassword |END| Successfully changed user password.");
		
	}

}
