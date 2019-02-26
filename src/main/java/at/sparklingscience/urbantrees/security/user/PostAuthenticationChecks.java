package at.sparklingscience.urbantrees.security.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

public class PostAuthenticationChecks implements UserDetailsChecker {

	@Override
	public void check(UserDetails toCheck) {
	}

}
