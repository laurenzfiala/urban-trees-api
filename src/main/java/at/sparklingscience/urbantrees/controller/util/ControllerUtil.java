package at.sparklingscience.urbantrees.controller.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;
import at.sparklingscience.urbantrees.security.authentication.jwt.JWTAuthenticationToken;

/**
 * Contains utility methods for the controllers.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/30
 */
@Component
public class ControllerUtil {

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
	
	/**
	 * Check if the given principal (user) is logged in anonymously.
	 * @param authentication DI authentication object from controller method.
	 * @return true if the given user is anonymously logged-in; false otherwise.
	 */
	public static boolean isUserAnonymous(Authentication authentication) {
		return authentication == null || !(authentication instanceof JWTAuthenticationToken);
	}

	public static AuthenticationToken getAuthToken(Authentication authentication) {
		return (AuthenticationToken) authentication;
	}
	
	/**
	 * Converts first 5 lines of the given stacktrace to string.
	 * @param trace stacktrace of a message
	 * @return string, each trace separated by a newline
	 */
	public static String traceToString(StackTraceElement[] trace) {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < 5; i++) {
			str.append(trace[i].toString());
			str.append('\n');
		}
		return str.toString();
	}
	
	/**
	 * returns a new array with the current users' id added to it at index 0.
	 * Returned array has length of input.length + 1
	 * If input is null, return empty array.
	 */
	public static int[] addUserIdToIntArray(int[] input, Authentication auth) {
		
		if (input == null) {
			return new int[0];
		}
		
		int[] assocUsers = new int[input.length + 1];
		assocUsers[0] = ControllerUtil.getAuthToken(auth).getId();
		System.arraycopy(input, 0, assocUsers, 1, input.length);
		
		return assocUsers;
		
	}
	
}
