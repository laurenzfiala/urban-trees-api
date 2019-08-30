package at.sparklingscience.urbantrees.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;

/**
 * Only the identifying information of a user.
 * 
 * @author Laurenz Fiala
 * @since 2019/08/08
 */
public class UserIdentity {
	
	/**
	 * Users' unique identifier.
	 */
	@Min(value = 1, groups = {ValidationGroups.Read.class})
	private int id;
	
	/**
	 * Users' username to authenticate.
	 * The value is encrypted.
	 */
	@NotNull
	private String username;
	
	public UserIdentity() {}
	
	public UserIdentity(int id, String username) {
		this.id = id;
		this.username = username;
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

	public static UserIdentity fromUser(User user) {
		return new UserIdentity(user.getId(), user.getUsername());
	}
	
}
