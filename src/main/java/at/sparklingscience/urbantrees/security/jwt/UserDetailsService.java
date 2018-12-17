package at.sparklingscience.urbantrees.security.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import at.sparklingscience.urbantrees.security.user.AuthenticationService;
import at.sparklingscience.urbantrees.security.user.User;

/**
 * Checks the database for valid credentials.
 * 
 * @author Laurenz Fiala
 * @since 2018/06/10
 */
@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
	
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsService.class);
	
	@Autowired
	private AuthenticationService authenticationService;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		LOGGER.trace("Searching for user {} in the database.", username);
		User user = this.authenticationService.findUser(username);
		if (user == null) {
			LOGGER.trace("Could not find user {}.", username);
			throw new UsernameNotFoundException("Could not find user with name " + user);
		}
		
		LOGGER.trace("User {} found.", username);
		return new org.springframework.security.core.userdetails.User(
				user.getUsername(),
				user.getPassword(),
				user.isActive(),
				true,
				user.isCredentialsNonExpired(),
				true,
				AuthorityUtils.createAuthorityList(user.getRoles().toArray(new String[0]))
				);
	}

}
