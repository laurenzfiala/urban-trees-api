package at.sparklingscience.urbantrees.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

// TODO remove
public class PostAuthenticationChecks implements UserDetailsChecker {

	@Override
	public void check(UserDetails toCheck) {
	}

}
