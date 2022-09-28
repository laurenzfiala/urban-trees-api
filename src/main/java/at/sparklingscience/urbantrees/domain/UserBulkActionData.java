package at.sparklingscience.urbantrees.domain;

import java.util.List;

/**
 * Holds all possible additional data
 * needed for some of the user bulk actions.
 * Therefore, only one/some of the fields will be
 * populated on a request.
 * 
 * @author Laurenz Fiala
 * @since 2021/02/20
 */
public class UserBulkActionData {
	
	/**
	 * Roles used to add/remove roles.
	 */
	private List<Role> roles;

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

}
