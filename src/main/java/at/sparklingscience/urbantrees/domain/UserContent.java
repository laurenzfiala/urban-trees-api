package at.sparklingscience.urbantrees.domain;

import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.validator.annotation.DateRange;
import at.sparklingscience.urbantrees.domain.validator.annotation.DateRange.Range;

/**
 * Holds user content.
 * This is one user content entry as soon as an entry is approved.
 * Any changes after that ware stored as a new entry in the DB and have to be re-approved.
 * Query for {@link #historyId} to find the previous content (if exists).
 * 
 * @author Laurenz Fiala
 * @since 2020/03/17
 */
public class UserContent {

	@Min(1)
	private int id;
	
	@NotNull
	private String contentId;
	
	private int contentOrder;
	
	@NotNull
	@NotEmpty
	private String contentTitle;
	
	@NotNull
	private UserContentLanguage contentLang;
	
	@NotNull
	private String content;
	
	private boolean isDraft;
	
	private boolean isShown;
	
	@NotNull
	@DateRange(Range.PAST_AND_PRESENT)
	private Date saveDat;
	
	@Min(1)
	private int historyId;
	
	@Min(1)
	private int userId;
	
	@NotNull
	@DateRange(Range.PAST_AND_PRESENT)
	private int approveDat;
	
	@Min(1)
	private int approveUserId;
	
	/**
	 * Whether or not the requesting user may edit this content.
	 */
	private boolean isEditable = false;

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

	public UserContentLanguage getContentLang() {
		return contentLang;
	}

	public void setContentLang(UserContentLanguage contentLang) {
		this.contentLang = contentLang;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isDraft() {
		return isDraft;
	}

	public void setDraft(boolean isDraft) {
		this.isDraft = isDraft;
	}

	public boolean isShown() {
		return isShown;
	}

	public void setShown(boolean isShown) {
		this.isShown = isShown;
	}

	public Date getSaveDat() {
		return saveDat;
	}

	public void setSaveDat(Date saveDat) {
		this.saveDat = saveDat;
	}

	public int getHistoryId() {
		return historyId;
	}

	public void setHistoryId(int historyId) {
		this.historyId = historyId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getApproveDat() {
		return approveDat;
	}

	public void setApproveDat(int approveDat) {
		this.approveDat = approveDat;
	}

	public int getApproveUserId() {
		return approveUserId;
	}

	public void setApproveUserId(int approveUserId) {
		this.approveUserId = approveUserId;
	}

	public boolean isEditable() {
		return isEditable;
	}

	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}

}
