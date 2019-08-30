package at.sparklingscience.urbantrees.domain;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * An application event (= log).
 * 
 * @author Laurenz Fiala
 * @since 2019/07/27
 */
public class Event implements Serializable {

	private static final long serialVersionUID = 20190727L;

	/**
	 * Event' unique identifier.
	 */
	@Min(value = 1)
	private int id;
	
	/**
	 * Brief surrary of event.
	 */
	@NotNull
	private String message;
	
	/**
	 * Event details.
	 */
	@NotNull
	private String details;
	
	/**
	 * Event severity.
	 */
	@NotNull
	private EventSeverity severity;
	
	/**
	 * Event date.
	 */
	@NotNull
	private Date eventDate;
	
	/**
	 * Optional remark regading this event.
	 */
	@NotNull
	private String remark;

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

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public EventSeverity getSeverity() {
		return severity;
	}

	public void setSeverity(EventSeverity severity) {
		this.severity = severity;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Date getEventDate() {
		return eventDate;
	}

	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}
	
}
