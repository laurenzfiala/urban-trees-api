package at.sparklingscience.urbantrees.security.user;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;

/**
 * A user for authentication.
 * 
 * @author Laurenz Fiala
 * @since 2018/06/09
 */
public class User implements Serializable {

	private static final long serialVersionUID = 20181205L;

	/**
	 * Users' unique identifier.
	 */
	@Min(value = 1, groups = {ValidationGroups.Read.class})
	private int id;
	
	/**
	 * Users' username to authenticate. 
	 */
	@NotNull
	private String username;
	
	/**
	 * Users' password to authenticate.
	 */
	@NotNull
	private String password;
	
	/**
	 * Whether the users' account is currently active or inactive.
	 */
	@NotNull(groups = {ValidationGroups.Read.class})
	private boolean isActive;
	
	/**
	 * Whether the users' credentials are to be replaced or if they are fine.
	 */
	@NotNull(groups = {ValidationGroups.Read.class})
	private boolean isCredentialsNonExpired;
	
	/**
	 * Date of users' last login.
	 */
	@NotNull(groups = {ValidationGroups.Read.class})
	private Date lastLoginDate;
	
	/**
	 * All roles assigned to this user.
	 */
	@NotNull(groups = {ValidationGroups.Read.class})
	private List<String> roles;
	
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isCredentialsNonExpired() {
		return isCredentialsNonExpired;
	}

	public void setCredentialsNonExpired(boolean isCredentialsNonExpired) {
		this.isCredentialsNonExpired = isCredentialsNonExpired;
	}
	
	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	
}
