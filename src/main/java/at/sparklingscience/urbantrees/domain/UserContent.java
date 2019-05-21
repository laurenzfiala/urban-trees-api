package at.sparklingscience.urbantrees.domain;

import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Holds user content.
 * 
 * @author Laurenz Fiala
 * @since 2018/02/18
 */
public class UserContent {

	@Min(1)
	private int id;
	
	@NotNull
	private String content;
	
	@NotNull
	private UserContentLanguage language;
	
	@NotNull
	private UserContentTag tag;
	
	@NotNull
	private Date modDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getModDate() {
		return modDate;
	}

	public void setModDate(Date modDate) {
		this.modDate = modDate;
	}

	public UserContentTag getTag() {
		return tag;
	}

	public void setTag(UserContentTag tag) {
		this.tag = tag;
	}

	public UserContentLanguage getLanguage() {
		return language;
	}

	public void setLanguage(UserContentLanguage language) {
		this.language = language;
	}

}
