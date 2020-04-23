package at.sparklingscience.urbantrees.security.authentication;

import java.util.Date;

import org.springframework.security.core.Authentication;

/**
 * Interface for user auth credentials DTOs.
 * Defines additional methods needed by controllers, services etc.
 * 
 * @author Laurenz Fiala
 * @since 2020/04/23
 */
public interface AuthenticationToken extends Authentication {
	
	/**
	 * Return users' id.
	 */
	int getId();
	
	/**
	 * Return tokens' session id.
	 */
	long getAuthId();
	
	/**
	 * Return the tokens creation date.
	 */
	Date getTokenCreationDate();
	
}
