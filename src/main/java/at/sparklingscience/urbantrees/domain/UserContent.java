package at.sparklingscience.urbantrees.domain;

import javax.validation.constraints.NotNull;

/**
 * Holds a single user content entry.
 * 
 * @author Laurenz Fiala
 * @since 2020/12/05
 */
public class UserContent extends UserContentMetadata {
	
	@NotNull
	private String content;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
}
