package at.sparklingscience.urbantrees.domain;

import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.validator.annotation.DateRange;

/**
 * Holds a single user content entry.
 * This light version does not contain the actual content, only the metadata.
 * 
 * @author Laurenz Fiala
 * @since 2020/12/10
 */
public class UserContentMetadata {

	@Min(1)
	private long id;
	
	@NotNull
	private String contentId;
	
	@NotNull
	private int contentOrder;
	
	private String contentTitle;
	
	@NotNull
	private UserContentLanguage contentLanguage;
	
	private boolean isDraft;
	
	@NotNull
	@DateRange(DateRange.Range.PAST_AND_PRESENT)
	private Date saveDate;
	
	@Min(1)
	private Long historyId;
	
	private UserIdentity user;
	
	private Date approveDate;
	
	private UserIdentity approveUser;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public int getContentOrder() {
		return contentOrder;
	}

	public void setContentOrder(int contentOrder) {
		this.contentOrder = contentOrder;
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

	public Long getHistoryId() {
		return historyId;
	}

	public void setHistoryId(Long historyId) {
		this.historyId = historyId;
	}
	
}
