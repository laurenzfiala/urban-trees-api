package at.sparklingscience.urbantrees.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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
	
}
