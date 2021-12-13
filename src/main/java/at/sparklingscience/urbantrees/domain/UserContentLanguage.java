package at.sparklingscience.urbantrees.domain;

import java.util.Objects;

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
	
	public UserContentLanguage() {}
	
	public UserContentLanguage(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public static final UserContentLanguage fromId(String id) {
		
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("content language id is invalid");
		}
		return new UserContentLanguage(id);
	
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserContentLanguage other = (UserContentLanguage) obj;
		return Objects.equals(id, other.id);
	}

}
