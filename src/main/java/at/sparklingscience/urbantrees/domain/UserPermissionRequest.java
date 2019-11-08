package at.sparklingscience.urbantrees.domain;

import javax.validation.constraints.NotNull;

/**
 * Used to request a new shared permission for a user.
 * 
 * @author Laurenz Fiala
 * @since 2019/08/08
 */
public class UserPermissionRequest {

	@NotNull
	private String username;
	
	@NotNull
	private String ppin;
	
	@NotNull
	private UserPermission permission;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public UserPermission getPermission() {
		return permission;
	}
	public void setPermission(UserPermission permission) {
		this.permission = permission;
	}
	public String getPpin() {
		return ppin;
	}
	public void setPpin(String ppin) {
		this.ppin = ppin;
	}
	
}
