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
	private String contentPath;
	
	private String contentTitle;
	
	@NotNull
	private UserContentLanguage contentLanguage;
	
	@NotNull
	private UserContentStatus status;
	
	@NotNull
	@DateRange(DateRange.Range.PAST_AND_PRESENT)
	private Date saveDate;
	
	@Min(1)
	private Long historyId;
	
	@Min(1)
	private Long previousId;
	
	@Min(1)
	private Long nextId;
	
	private UserIdentity user;
	
	private Date approveDate;
	
	private UserIdentity approveUser;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getContentPath() {
		return contentPath;
	}

	public void setContentPath(String contentId) {
		this.contentPath = contentId;
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

	public Long getPreviousId() {
		return previousId;
	}

	public void setPreviousId(Long previousId) {
		this.previousId = previousId;
	}

	public Long getNextId() {
		return nextId;
	}

	public void setNextId(Long nextId) {
		this.nextId = nextId;
	}

	public UserContentStatus getStatus() {
		return status;
	}

	public void setStatus(UserContentStatus status) {
		this.status = status;
	}
	
}
