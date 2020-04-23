package at.sparklingscience.urbantrees.security.authentication.user;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * TODO
 * 
 * @author Laurenz Fiala
 * @since 2020/04/23
 */
public class UserAuthenticationToken implements Authentication {

	private static final long serialVersionUID = 20200423L;
	
	private boolean isAuthenticated = false;

	private String username;

	private String password;
	
	public UserAuthenticationToken(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public String getName() {
		return this.username;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return null;
	}

	@Override
	public Object getCredentials() {
		return this.password;
	}

	@Override
	public Object getDetails() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return null;
	}

	@Override
	public boolean isAuthenticated() {
		return this.isAuthenticated;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		this.isAuthenticated = isAuthenticated;
	}
	
}
