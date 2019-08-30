package at.sparklingscience.urbantrees.domain;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;

/**
 * An application report.
 * 
 * @author Laurenz Fiala
 * @since 2019/07/27
 */
public class Report implements Serializable {

	private static final long serialVersionUID = 20190727L;

	/**
	 * Reports' unique identifier.
	 */
	@Min(value = 1, groups = {ValidationGroups.Read.class})
	private int id;
	
	/**
	 * Roles' name.
	 */
	@NotNull
	@NotEmpty
	private String message;
	
	/**
	 * Optional event id associated.
	 */
	private Event assocEvent;
	
	/**
	 * Optional associated user id.
	 */
	private Integer userId;
	
	/**
	 * Whether the report as auto-generated or not.
	 */
	private boolean autoCreate;
	
	/**
	 * Whether the report is marked as resolved or not.
	 */
	private boolean resolved;
	
	/**
	 * Roles' name.
	 */
	@NotNull(groups = {ValidationGroups.Read.class})
	private String remark;
	
	/**
	 * TIme of report creation.
	 */
	@NotNull(groups = {ValidationGroups.Read.class})
	private Date reportDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public boolean isAutoCreate() {
		return autoCreate;
	}

	public void setAutoCreate(boolean autoCreate) {
		this.autoCreate = autoCreate;
	}

	public boolean isResolved() {
		return resolved;
	}

	public void setResolved(boolean resolved) {
		this.resolved = resolved;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Date getReportDate() {
		return reportDate;
	}

	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}

	public Event getAssocEvent() {
		return assocEvent;
	}

	public void setAssocEvent(Event assocEvent) {
		this.assocEvent = assocEvent;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
}
