package at.sparklingscience.urbantrees.domain;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Holds a template user and all usernames to register
 * using the given template.
 * 
 * @author Laurenz Fiala
 * @since 2021/02/16
 */
public class UserCreation implements Serializable {
	
	private static final long serialVersionUID = 20210216L;
	
	/**
	 * Template off which to base each new
	 * user given in {@link #usernames}.
	 */
	@NotNull
	private UserLight template;
	
	/**
     * Users to create. For each
     * array element, one user is created.
     */
	@NotNull
	@NotEmpty
	private List<String> usernames;

	public UserLight getTemplate() {
		return template;
	}

	public void setTemplate(UserLight template) {
		this.template = template;
	}

	public List<String> getUsernames() {
		return usernames;
	}

	public void setUsernames(List<String> usernames) {
		this.usernames = usernames;
	}

	@Override
	public String toString() {
		return String.join(", ", this.usernames);
	}
	
}
