package at.sparklingscience.urbantrees.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import at.sparklingscience.urbantrees.controller.util.ControllerUtil;
import at.sparklingscience.urbantrees.domain.OtpCode;
import at.sparklingscience.urbantrees.domain.PasswordReset;
import at.sparklingscience.urbantrees.domain.UserIdentity;
import at.sparklingscience.urbantrees.domain.UserPermission;
import at.sparklingscience.urbantrees.domain.UserPermissionRequest;
import at.sparklingscience.urbantrees.domain.UsernameChange;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;
import at.sparklingscience.urbantrees.security.authentication.otp.OtpValidationException;
import at.sparklingscience.urbantrees.service.AccountService;
import at.sparklingscience.urbantrees.service.AuthenticationService;
import io.nayuki.qrcodegen.QrCode;

/**
 * Handles actions on user accounts.
 * 
 * @author Laurenz Fiala
 * @since 2018/07/13
 */
@RestController
@RequestMapping("/account")
public class AccountController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

	@Autowired
	private AccountService accountService;
	
	@Autowired
	private AuthenticationService authService;
	
	@Value("${at.sparklingscience.urbantrees.otpIssuer}")
	private String otpIssuer;
	
	@RequestMapping(method = RequestMethod.PUT, path = "/changepassword")
	public void putChangePassword(@RequestBody PasswordReset passwordReset, Authentication auth) {
		
		final AuthenticationToken authToken = ControllerUtil.getAuthToken(auth);
		
		LOGGER.debug("[[ PUT ]] putChangePassword - reset password for user: {}", authToken.getDetails());
		
		this.accountService.changePassword(authToken.getId(), authToken.getAuthorities(), passwordReset);
		this.authService.userCredentialsChanged(authToken);
		
		LOGGER.debug("[[ PUT ]] putChangePassword |END| Successfully changed user password.");
		
	}

	@RequestMapping(method = RequestMethod.PUT, path = "/changeusername")
	public void putChangeUsername(@RequestBody UsernameChange usernameChange, Authentication auth) {
		
		final AuthenticationToken authToken = ControllerUtil.getAuthToken(auth);
		
		LOGGER.debug("[[ PUT ]] putChangeUsername - change username for user: {} to: {}", authToken.getDetails(), usernameChange.getUsername());
		
		this.accountService.changeUsername(authToken.getId(), usernameChange);
		this.authService.userCredentialsChanged(authToken);
		
		LOGGER.debug("[[ PUT ]] putChangeUsername |END| Successfully changed username.");
		
	}

	@RequestMapping(method = RequestMethod.POST, path = "/permission/request")
	public UserIdentity postUserPermissionRequest(@RequestBody UserPermissionRequest permRequest, Authentication auth) {
		
		final AuthenticationToken authToken = ControllerUtil.getAuthToken(auth);

		LOGGER.debug(
			"[[ POST ]] postUserPermissionRequest - add permission: {} from user: {} to user: {}",
			permRequest.getPermission(),
			permRequest.getUsername(),
			authToken.getName()
		);

		UserIdentity ui = this.accountService.requestUserPermission(permRequest, authToken);
		
		LOGGER.debug(
			"[[ POST ]] postUserPermissionRequest |END| Successfully added permission: {} from user: {} to user: {}",
			permRequest.getPermission(),
			permRequest.getUsername(),
			authToken.getName()
		);
		
		return ui;
		
	}

	@RequestMapping(method = RequestMethod.GET, path = "/permission/granted/{permission}")
	public List<UserIdentity> getUsersGrantingPermission(@PathVariable UserPermission permission, Authentication auth) {
		
		final int receivingUserId = ControllerUtil.getAuthToken(auth).getId();

		LOGGER.debug(
			"[[ GET ]] getUsersGrantingPermission - get granting users for receiving user: {} with perm: {}",
			receivingUserId,
			permission
		);

		List<UserIdentity> grantingUsers = this.accountService.getUsersGrantingPermission(receivingUserId, permission);
		
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
		
		return this.accountService.newPermissionsPIN(userId);
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/otp")
	public boolean getUsingOtp(Authentication auth) {
		
		final int userId = ControllerUtil.getAuthToken(auth).getId();
		LOGGER.debug("[[ GET ]] getUsingOtp - check OTP usage for user: {}", userId);
		
		return this.accountService.isUsingOtp(userId);
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/otp/activate", produces = "image/svg+xml")
	public String getNewOtp(Authentication auth) {
		
		final int userId = ControllerUtil.getAuthToken(auth).getId();
		LOGGER.debug("[[ GET ]] getNewOtp - generate new OTP credentials for user: {}", userId);
		
		final String uriPrefix = "otpauth://totp/";
		final String username = auth.getName();
		final String secret = this.accountService.initNewOtp(userId);
		final String issuer = this.otpIssuer;
		final QrCode qrCode = QrCode.encodeText(uriPrefix + username + "?secret=" + secret + "&issuer=" + issuer, QrCode.Ecc.HIGH);
		
		try {
			return qrCode.toSvgString(0);
		} finally {
			LOGGER.debug("[[ GET ]] getNewOtp |END| Successfully generated new OTP credentials & svg for user: {}", userId);
		}
		
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/otp/activate")
	public String[] postActivateOtp(@RequestBody OtpCode code, Authentication auth) {
		
		final AuthenticationToken authToken = ControllerUtil.getAuthToken(auth);
		LOGGER.debug("[[ POST ]] postActivateOtp - activate OTP for user: {}", authToken.getId());
		
		String[] scratchCodes;
		try {
			scratchCodes = this.accountService.validateNewOtp(authToken.getId(), code.getCode());
			this.authService.userCredentialsChanged(authToken);
		} catch (OtpValidationException e) {
			throw new BadRequestException(e.getMessage());
		}
		
		LOGGER.debug("[[ POST ]] postActivateOtp |END| Successfully activated OTP for user: {}", authToken.getId());
		return scratchCodes;
		
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/otp/deactivate")
	public void postDeactivateOtp(@RequestBody OtpCode code, Authentication auth) {
		
		final AuthenticationToken authToken = ControllerUtil.getAuthToken(auth);
		LOGGER.debug("[[ POST ]] postDeactivateOtp - deactivate OTP for user: {}", authToken.getId());
		
		try {
			this.accountService.deactivateOtp(authToken.getId(), code.getCode());
			this.authService.userCredentialsChanged(authToken);
		} catch (OtpValidationException e) {
			throw new BadRequestException("Invalid OTP.");
		}
		
		LOGGER.debug("[[ POST ]] postDeactivateOtp |END| Successfully deactivated OTP for user: {}", authToken.getId());
		
	}

}
