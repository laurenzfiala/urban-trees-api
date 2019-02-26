package at.sparklingscience.urbantrees.domain.ui;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;

/**
 * Announcement to show in the UI for
 * e.g. upcoming deployments etc.
 * 
 * @author Laurenz Fiala
 * @since 2018/02/22
 */
public class Announcement implements Serializable {
	
	private static final long serialVersionUID = 20180307L;

	/**
	 * Incrementing ID of the announcement.
	 */
	@Min(value = 1, groups = {ValidationGroups.Read.class})
	private int id;
	
	/**
	 * Announcement title.
	 */
	@NotNull
	@NotEmpty
	private String title;
	
	/**
	 * Announcement description.
	 */
	@NotNull
	@NotEmpty
	private String description;
	
	/**
	 * Severity can be used by the UI to highlight
	 * important messages and are used for ordering.
	 */
	private short severity;
	
	/**
	 * Date from which on to display the announcement.
	 */
	@NotNull
	private Date displayFromDate;
	
	/**
	 * Date up to which to display the announcement.
	 */
	@NotNull
	private Date displayToDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public short getSeverity() {
		return severity;
	}

	public void setSeverity(short severity) {
		this.severity = severity;
	}

	public Date getDisplayFromDate() {
		return displayFromDate;
	}

	public void setDisplayFromDate(Date displayFromDate) {
		this.displayFromDate = displayFromDate;
	}

	public Date getDisplayToDate() {
		return displayToDate;
	}

	public void setDisplayToDate(Date displayToDate) {
		this.displayToDate = displayToDate;
	}

}
