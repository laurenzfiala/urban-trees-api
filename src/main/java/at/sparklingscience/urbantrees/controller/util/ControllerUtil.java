package at.sparklingscience.urbantrees.controller.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.sparklingscience.urbantrees.exception.BadRequestException;

/**
 * Contains utility methods for the controllers.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/30
 */
public class ControllerUtil {
	
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerUtil.class);

	/**
	 * 
	 */
	public static Timespan getTimespanParams(String dateFormatPattern, String timespanMin, String timespanMax)
			throws BadRequestException {

		try {

			DateFormat dateFormat = new SimpleDateFormat(dateFormatPattern);

			Date timespanMinDate = null;
			if (timespanMin != null) {
				timespanMinDate = dateFormat.parse(timespanMin);
				LOGGER.debug("Using timespan min of {}", timespanMin);
			}

			Date timespanMaxDate = null;
			if (timespanMax != null) {
				timespanMaxDate = dateFormat.parse(timespanMax);
				LOGGER.debug("Using timespan max of {}", timespanMax);
			}

			return new Timespan(timespanMinDate, timespanMaxDate);

		} catch (ParseException e) {
			LOGGER.error("Timestamps could not be parsed: " + e.getMessage(), e);
			throw new BadRequestException("Timestamps could not be parsed.");
		}

	}

}
