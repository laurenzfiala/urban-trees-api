package at.sparklingscience.urbantrees.security;

import java.util.Collection;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * API key {@link Authentication} class for spring security.
 * 
 * @see Authentication
 * @author Laurenz Fiala
 * @since 2017/12/26
 */
public class ApiKeyAuthentication implements Authentication {
	
	/**
	 * Serial. Change when updated.
	 */
	private static final long serialVersionUID = 20171230L;

	/**
	 * The {@link UUID} api key used to authenticate.
	 * @see UUID
	 */
	private UUID apiKey;
	
	/**
	 * Whether {@link #apiKey} is trusted or not.
	 */
	private boolean isAuthenticated = false;

	public ApiKeyAuthentication(UUID apiKey) {
		this.apiKey = apiKey;
	}
	
	public ApiKeyAuthentication(boolean isAuthenticated) {
		this.isAuthenticated = isAuthenticated;
	}
	
	public ApiKeyAuthentication(UUID apiKey, boolean isAuthenticated) {
		this(apiKey);
		this.isAuthenticated = isAuthenticated;
	}
	
	@Override
	public String getName() {
		return null;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return null;
	}

	@Override
	public Object getCredentials() {
		return this.apiKey;
	}

	@Override
	public Object getDetails() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return null;
	}

	@Override
	public boolean isAuthenticated() {
		if (!this.isAuthenticated || this.apiKey == null) {
			return false;
		}
		return true;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		this.isAuthenticated = isAuthenticated;
	}

}
