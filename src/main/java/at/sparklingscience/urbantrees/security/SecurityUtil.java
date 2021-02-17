package at.sparklingscience.urbantrees.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import at.sparklingscience.urbantrees.SecurityConfiguration;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;

/**
 * Helper methods regarding security functionality.
 * 
 * @author Laurenz Fiala
 * @since 2019/02/07
 */
public class SecurityUtil {

	/**
	 * Converts a non-prefixed role to one with the required prefix.
	 * @param roleName role name
	 * @return role, like "ROLE_USER"
	 */
	public static String role(final String roleName) {
    	return "ROLE_" + roleName;
    }
	
	/**
	 * Create a {@link SimpleGrantedAuthority} from the
	 * given role (needs to be non-prefixed). 
	 * @param role role without prefix
	 * @return {@link SimpleGrantedAuthority}
	 */
	public static GrantedAuthority grantedAuthority(final String role) {
    	return new SimpleGrantedAuthority(role(role));
    }
	
	/**
	 * Check that the given userAuthorities are a subset of checkAuthorities
	 * and return false if this is not the case.
	 * @param userAuthorities granted authorities
	 * @param checkAuthorities authorities that have to be contained in userAuthorities
	 * @return true if checkAuthorities is a subset of userAuthorities; false otherwise.
	 */
	public static boolean hasAllAuthorities(Collection<? extends GrantedAuthority> userAuthorities, Collection<? extends GrantedAuthority> checkAuthorities) {
		return userAuthorities
				.stream()
				.allMatch(ua -> checkAuthorities.stream().anyMatch(ca -> ua.getAuthority().equals(ca.getAuthority())));
	}
	
	/**
	 * Returns true if the given user is an admin.
	 */
	public static boolean isAdmin(AuthenticationToken authToken) {
		return authToken.getAuthorities()
				.stream()
				.anyMatch(a -> a.equals(grantedAuthority(SecurityConfiguration.ADMIN_ACCESS_ROLE)));
	}
	
}
