package at.sparklingscience.urbantrees.domain;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

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
	 * Holds filters which define which users are affected.
	 */
	@NotNull
	private Map<String, Object> filters;
	
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

	public Map<String, Object> getFilters() {
		return filters;
	}

	public void setFilters(Map<String, Object> filters) {
		this.filters = filters;
	}

}
