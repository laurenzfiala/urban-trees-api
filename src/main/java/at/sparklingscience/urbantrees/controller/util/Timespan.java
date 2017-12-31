package at.sparklingscience.urbantrees.controller.util;

import java.util.Date;

/**
 * Class for simpler handling of timespan query parameters.
 * All members are immutable.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/30
 */
public class Timespan {

	/**
	 * Timespan start.
	 */
	private final Date start;
	
	/**
	 * Timespan end.
	 */
	private final Date end;
	
	public Timespan(Date start, Date end) {
		this.start = start;
		this.end = end;
	}

	public Date getStart() {
		return start;
	}

	public Date getEnd() {
		return end;
	}
	
}
