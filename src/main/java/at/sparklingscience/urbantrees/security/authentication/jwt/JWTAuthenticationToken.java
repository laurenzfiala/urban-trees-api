package at.sparklingscience.urbantrees.security.authentication.jwt;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;

import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;
import at.sparklingscience.urbantrees.security.user.User;

/**
 * DTO for transferring user auth properties from the client-side JWT.
 * Holds all information and claims needed for authenticating a request.
 * 
 * Also used for constructing JWT tokens.
 * 
 * @author Laurenz Fiala
 * @since 2020/04/19 (doc)
 */
public class JWTAuthenticationToken implements AuthenticationToken {

	private static final long serialVersionUID = 20190118L;

	private int id;

	private long authId;

	private String username;
	
	private String password;
	
	/**
	 * Holds all roles granted to the user.
	 */
	private Collection<? extends GrantedAuthority> authorities;

	private boolean isAuthenticated = true;
	
	private User details;
	
	/**
	 * Date of JWT creation.
	 */
	private Date tokenCreationDate;
	
	public JWTAuthenticationToken(int id, String username, Collection<? extends GrantedAuthority> authorities) {
		this(id, 0, username, authorities, null, null);
	}
	
	public JWTAuthenticationToken(int id, String username, Collection<? extends GrantedAuthority> authorities, User details) {
		this(id, 0, username, authorities, details, null);
	}
	
	public JWTAuthenticationToken(int id, long authId, String username, Collection<? extends GrantedAuthority> authorities, Date tokenCreationDate) {
		this(id, authId, username, authorities, null, tokenCreationDate);
	}

	public JWTAuthenticationToken(int id, long authId, String username, Collection<? extends GrantedAuthority> authorities, User details, Date tokenCreationDate) {
		this.id = id;
		this.authId = authId;
		this.username = username;
		this.authorities = authorities;
		this.details = details;
		this.tokenCreationDate = tokenCreationDate;
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

	public JWTAuthenticationToken withAuthorities(GrantedAuthority ...authorities) {
		this.setAuthorities(Arrays.asList(authorities));
		return this;
	}

	public Date getTokenCreationDate() {
		return tokenCreationDate;
	}

	public void setTokenCreationDate(Date tokenCreationDate) {
		this.tokenCreationDate = tokenCreationDate;
	}

	public long getAuthId() {
		return authId;
	}

	public void setAuthId(long authId) {
		this.authId = authId;
	}
	
}
