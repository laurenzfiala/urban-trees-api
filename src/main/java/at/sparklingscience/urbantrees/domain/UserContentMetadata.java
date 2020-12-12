package at.sparklingscience.urbantrees.domain;

import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Holds a single user content entry.
 * This light version does not contain the actual content, only the metadata.
 * 
 * @author Laurenz Fiala
 * @since 2020/12/10
 */
public class UserContentMetadata {

	@Min(1)
	private int id;
	
	@NotNull
	private String contentId;
	
	private String contentTitle;
	
	@NotNull
	private UserContentLanguage contentLanguage;
	
	private boolean isDraft;
	
	@NotNull
	private Date saveDate;
	
	private UserIdentity user;
	
	private Date approveDate;
	
	private UserIdentity approveUser;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public String getContentTitle() {
		return contentTitle;
	}

	public void setContentTitle(String contentTitle) {
		this.contentTitle = contentTitle;
	}

	public UserContentLanguage getContentLanguage() {
		return contentLanguage;
	}

	public void setContentLanguage(UserContentLanguage contentLanguage) {
		this.contentLanguage = contentLanguage;
	}

	public boolean isDraft() {
		return isDraft;
	}

	public void setDraft(boolean isDraft) {
		this.isDraft = isDraft;
	}

	public Date getSaveDate() {
		return saveDate;
	}

	public void setSaveDate(Date saveDate) {
		this.saveDate = saveDate;
	}

	public UserIdentity getUser() {
		return user;
	}

	public void setUser(UserIdentity user) {
		this.user = user;
	}

	public Date getApproveDate() {
		return approveDate;
	}

	public void setApproveDate(Date approveDate) {
		this.approveDate = approveDate;
	}

	public UserIdentity getApproveUser() {
		return approveUser;
	}

	public void setApproveUser(UserIdentity approveUser) {
		this.approveUser = approveUser;
	}
	
}
