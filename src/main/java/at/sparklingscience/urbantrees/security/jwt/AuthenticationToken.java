package at.sparklingscience.urbantrees.security.jwt;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import at.sparklingscience.urbantrees.security.user.User;

public class AuthenticationToken implements Authentication {

	private static final long serialVersionUID = 20190118L;

	private int id;

	private String username;
	
	private String password;
	
	private Collection<? extends GrantedAuthority> authorities;

	private boolean isAuthenticated = true;
	
	private User details;
	
	private Date tokenCreationDate;
	
	public AuthenticationToken(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public AuthenticationToken(int id, String username, Collection<? extends GrantedAuthority> authorities) {
		this(id, username, authorities, null);
	}

	public AuthenticationToken(int id, String username, Collection<? extends GrantedAuthority> authorities, Date tokenCreationDate) {
		this.id = id;
		this.username = username;
		this.authorities = authorities;
		this.tokenCreationDate = tokenCreationDate;
	}
	
	public AuthenticationToken(String username, String password, Collection<? extends GrantedAuthority> authorities) {
		this(-1, username, authorities);
		this.password = password;
	}
	
	public void updateByUser(User user) {
		this.setId(user.getId());
		this.setUsername(user.getUsername());
	}
	
	@Override
	public String getName() {
		return this.username;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	@Override
	public Object getCredentials() {
		return this.password;
	}

	@Override
	public Object getDetails() {
		return this.details;
	}

	@Override
	public Object getPrincipal() {
		return this;
	}

	@Override
	public boolean isAuthenticated() {
		return this.isAuthenticated;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		this.isAuthenticated = isAuthenticated;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setDetails(User details) {
		this.details = details;
	}

	public AuthenticationToken withAuthorities(GrantedAuthority ...authorities) {
		this.setAuthorities(Arrays.asList(authorities));
		return this;
	}

	public Date getTokenCreationDate() {
		return tokenCreationDate;
	}

	public void setTokenCreationDate(Date tokenCreationDate) {
		this.tokenCreationDate = tokenCreationDate;
	}
	
}
