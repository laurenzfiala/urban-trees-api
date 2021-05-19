package at.sparklingscience.urbantrees.security;

import java.util.Collection;
import java.util.LinkedList;

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
	 * @param roleName role without prefix
	 * @return {@link SimpleGrantedAuthority}
	 */
	public static GrantedAuthority grantedAuthority(final String roleName) {
    	return new SimpleGrantedAuthority(role(roleName));
    }
	
	/**
	 * TODO
	 * @param authToken
	 * @param checkAuthorities
	 * @return
	 */
	public static boolean hasAnyAuthorityOrAdmin(AuthenticationToken authToken, Collection<? extends GrantedAuthority> checkAuthorities) {
		if (authToken == null) {
			return false;
		}
		Collection<GrantedAuthority> checkAuthoritiesAndAdmin = new LinkedList<>();
		checkAuthoritiesAndAdmin.add(SecurityUtil.grantedAuthority(SecurityConfiguration.ADMIN_ACCESS_ROLE));
		checkAuthoritiesAndAdmin.addAll(checkAuthorities);
		return hasAnyAuthority(authToken.getAuthorities(), checkAuthoritiesAndAdmin);
	}
	
	/**
	 * TODO
	 * @param userAuthorities
	 * @param checkAuthorities
	 * @return
	 */
	public static boolean hasAnyAuthority(Collection<? extends GrantedAuthority> userAuthorities, Collection<? extends GrantedAuthority> checkAuthorities) {
		return userAuthorities
				.stream()
				.anyMatch(ua -> checkAuthorities.stream().anyMatch(ca -> ua.getAuthority().equals(ca.getAuthority())));
	}
	
	/**
	 * Check that the given checkAuthorities are a subset of userAuthorities
	 * and return false if this is not the case.
	 * @param userAuthorities granted authorities
	 * @param checkAuthorities authorities that have to be contained in userAuthorities
	 * @return true if checkAuthorities is a subset of userAuthorities; false otherwise.
	 */
	public static boolean hasAllAuthorities(Collection<? extends GrantedAuthority> userAuthorities, Collection<? extends GrantedAuthority> checkAuthorities) {
		return checkAuthorities
				.stream()
				.allMatch(ca -> userAuthorities.stream().anyMatch(ua -> ua.getAuthority().equals(ca.getAuthority())));
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
