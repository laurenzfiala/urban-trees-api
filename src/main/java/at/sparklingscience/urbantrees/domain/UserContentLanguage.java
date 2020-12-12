package at.sparklingscience.urbantrees.domain;

import javax.validation.constraints.NotNull;

/**
 * A language for {@link UserContent}.
 * 
 * @author Laurenz Fiala
 * @since 2020/12/10 (doc)
 */
public class UserContentLanguage {

	@NotNull
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
