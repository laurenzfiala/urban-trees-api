package at.sparklingscience.urbantrees.domain;

import javax.validation.constraints.NotNull;

/**
 * A language for {@link UserContent}.
 * 
 * @author Laurenz Fiala
 * @since 2019/03/13
 */
public class UserContentLanguage {

	@NotNull
	private String id;
	
	@NotNull
	private String name;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
