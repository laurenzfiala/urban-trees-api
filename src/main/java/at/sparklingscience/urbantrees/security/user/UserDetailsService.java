package at.sparklingscience.urbantrees.security.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import at.sparklingscience.urbantrees.domain.User;
import at.sparklingscience.urbantrees.security.SecurityUtil;
import at.sparklingscience.urbantrees.security.authentication.otp.OtpValidationException;
import at.sparklingscience.urbantrees.service.AuthenticationService;

/**
 * Checks the database for valid credentials.
 * 
 * @author Laurenz Fiala
 * @since 2018/06/10
 */
@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsService.class);
	
	@Autowired
	private AuthenticationService authenticationService;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		LOGGER.trace("Searching for user {} in the database.", username);
		User user = this.authenticationService.findUser(username);
		if (user == null) {
			LOGGER.trace("Could not find user {}.", user);
			throw new UsernameNotFoundException("Could not find user with name " + user);
		}
		
		LOGGER.trace("User {} found.", user);
		
		return this.domainUserToSecUser(user);
		
	}
	
	/**
	 * The user provided a login token via a link. Find them and return the security user.
	 * @param token the given login token (from the login link)
	 * @return security user (wrapped in UserDetails interface)
	 * @throws BadCredentialsException if no user with the given key can be found or the key is expired.
	 */
	public UserDetails loadUserByLoginKey(String token) throws BadCredentialsException {
		
		LOGGER.trace("Searching for user by token in the database.");
		User user = this.authenticationService.findUserByLoginKey(token);
		if (user == null) {
			LOGGER.trace("Could not find user {}.", user);
			throw new BadCredentialsException("Could not find user with name " + user);
		}
		
		LOGGER.trace("User {} found.", user);
		
		return this.domainUserToSecUser(user);
		
	}
	
	/**
	 * Check validity of given OTP against credentials stored in the DB.
	 * It is guaranteed that, if no excpetion is thrown by this method, the OTP was valid.
	 * @param userId users' id
	 * @param otp user-entered OTP code
	 * @throws OtpValidationException if the validation failed
	 * @see AuthenticationService#validateOtp(int, String)
	 */
	public void validateUserOtp(final int userId, final String otp) throws OtpValidationException {
		
		this.authenticationService.validateOtp(userId, otp);
		
	}
	
	/**
	 * Initialize a new security user instance given the domain user.
	 * @param domainUser user
	 */
	private at.sparklingscience.urbantrees.security.user.User domainUserToSecUser(User domainUser) {
		return new at.sparklingscience.urbantrees.security.user.User(
				domainUser.getId(),
				domainUser.getUsername(),
				domainUser.getPassword() == null ? domainUser.getSecureLoginKey() : domainUser.getPassword(),
				domainUser.isActive(),
				true,
				domainUser.isCredentialsNonExpired(),
				this.authenticationService.isUserNonLocked(domainUser),
				SecurityUtil.rolesToGrantedAuthorities(domainUser.getRoles()),
				domainUser.isUsingOtp()
				);
	}

}
