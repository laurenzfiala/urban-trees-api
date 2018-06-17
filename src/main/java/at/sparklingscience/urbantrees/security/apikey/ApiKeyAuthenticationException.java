package at.sparklingscience.urbantrees.security.apikey;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception class.
 * Used for exceptions where a request was not correctly authenticated with an api key.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/27
 */
public class ApiKeyAuthenticationException extends AuthenticationException {
	
	private static final long serialVersionUID = 20171227L;

	public ApiKeyAuthenticationException(String msg) {
		super(msg);
	}

	public ApiKeyAuthenticationException(String msg, Throwable e) {
		super(msg, e);
	}


}
