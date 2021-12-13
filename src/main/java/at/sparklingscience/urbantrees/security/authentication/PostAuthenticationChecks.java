package at.sparklingscience.urbantrees.security.authentication;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

// TODO move locked/expired checking here
public class PostAuthenticationChecks implements UserDetailsChecker {

	@Override
	public void check(UserDetails toCheck) {
	}

}
