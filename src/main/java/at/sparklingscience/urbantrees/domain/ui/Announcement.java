package at.sparklingscience.urbantrees.domain.ui;

import java.io.Serializable;

import javax.validation.constraints.Min;

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
	@Min(1)
	private int id;
	
	/**
	 * Announcement title.
	 */
	private String title;
	
	/**
	 * Announcement description.
	 */
	private String description;
	
	/**
	 * Severity can be used by the UI to highlight
	 * important messages and are used for ordering.
	 */
	private short severity;

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

}
