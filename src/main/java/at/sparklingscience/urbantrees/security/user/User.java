package at.sparklingscience.urbantrees.security.user;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

/**
 * User used for internal authentication with Spring.
 * Adds an 'id'-field to the default User bean provided by Spring.
 * 
 * @author Laurenz Fiala
 * @since 2019/01/18
 */
public class User extends org.springframework.security.core.userdetails.User {

	private static final long serialVersionUID = 20190118L;
	
	private int id;

	public User(int id, String username, String password, boolean enabled, boolean accountNonExpired,
			boolean credentialsNonExpired, boolean accountNonLocked,
			Collection<? extends GrantedAuthority> authorities) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
		this.setId(id);
	}

	public User(int id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
		super(username, password, authorities);
		this.setId(id);
	}
	
	@Override
	public String toString() {
		return this.getId() + " / " + this.getUsername();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
}
