package at.sparklingscience.urbantrees.domain.ui;

import java.util.Date;

/**
 * One value for a time.
 * Used for time charts.
 * 
 * @author Laurenz Fiala
 * @since 2019/07/14
 */
public class DateIntValue {

	private Date time;
	
	private int value;

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	
	
}
