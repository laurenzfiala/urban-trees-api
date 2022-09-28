package at.sparklingscience.urbantrees.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import at.sparklingscience.urbantrees.SecurityConfiguration;
import at.sparklingscience.urbantrees.domain.Role;
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
	public static String role(@NotNull @NotBlank final String roleName) {
		if (roleName == null || roleName.isBlank()) {
			throw new RuntimeException("Role must not be null or blank.");
		}
    	return "ROLE_" + roleName;
    }
	
	/**
	 * Create a {@link SimpleGrantedAuthority} from the
	 * given role (needs to be non-prefixed). 
	 * @param roleName role without prefix
	 * @return {@link SimpleGrantedAuthority}
	 */
	public static GrantedAuthority grantedAuthority(@NotNull @NotBlank String roleName) {
    	return new SimpleGrantedAuthority(role(roleName));
    }
	
	/**
	 * Map the given {@link Role} to {@link GrantedAuthority}.
	 * @param role to map to granted authority
	 * @return granted authority
	 */
	public static GrantedAuthority roleToGrantedAuthority(@NotNull Role role) {
		return new SimpleGrantedAuthority(role.getName());
	}
	
	/**
	 * Map the given list of {@link Role}s to {@link GrantedAuthority}s.
	 * If any of the roles is null, they are omitted from the result collection.
	 * @param roles to map to granted authorities
	 * @return granted authorities
	 */
	public static Collection<GrantedAuthority> rolesToGrantedAuthorities(@NotNull List<Role> roles) {
		return roles.stream()
					.filter(r -> r != null)
					.map(r -> roleToGrantedAuthority(r))
					.collect(Collectors.toList());
	}
	
	/**
	 * Check whether the given user auth token includes the given authority.
	 * If the user is anonymous (= token is null) then this returns false.
	 * @param authToken users' token
	 * @param authority authority the given auth token must include
	 * 					(or null to return true)
	 * @return true if the token grants the authority (or the authority is
	 * 		   null); false if not
	 */
	public static boolean hasAuthority(@Nullable AuthenticationToken authToken,
									   @NotNull GrantedAuthority authority) {
		if (isAnonymous(authToken)) {
			return false;
		}
		return authToken.getAuthorities()
				.stream()
				.anyMatch(ua -> ua.getAuthority().equals(authority.getAuthority()));
	}
	
	/**
	 * Check whether the given user auth token includes at least one of the
	 * given authorities.
	 * If the user is anonymous (= token is null) then this returns false.
	 * @param authToken users' token
	 * @param authorities check if user hat at least one of these authorities
	 * @return true if the token grants 1 or more authorities; false if not
	 */
	public static boolean hasAnyAuthority(@Nullable AuthenticationToken authToken,
										  @NotNull Collection<? extends GrantedAuthority> authorities) {
		if (isAnonymous(authToken)) {
			return false;
		}
		return authToken.getAuthorities()
				.stream()
				.anyMatch(ua -> authorities.stream()
										   .anyMatch(ca -> ua.getAuthority().equals(ca.getAuthority()))
				);
	}
	
	/**
	 * Check that the given checkAuthorities are a subset of userAuthorities
	 * and return false if this is not the case.
	 * @param authToken users' token
	 * @param authorities authorities that have to be contained in userAuthorities
	 * @return true if checkAuthorities is a subset of userAuthorities; false otherwise
	 */
	public static boolean hasAllAuthorities(@Nullable AuthenticationToken authToken,
			  								@NotNull Collection<? extends GrantedAuthority> authorities) {
		return authorities
				.stream()
				.allMatch(ca -> authToken.getAuthorities().stream()
														  .anyMatch(ua -> ua.getAuthority().equals(ca.getAuthority()))
			    );
	}
	
	/**
	 * Check whether the given user auth token includes the given role.
	 * @param authToken users' token
	 * @param role role the given auth token must include
	 * @return true if the token grants the role or role is null; false if not
	 * @see #hasAuthority(AuthenticationToken, GrantedAuthority)
	 */
	public static boolean hasRole(@Nullable AuthenticationToken authToken, Role role) {
		if (role == null) {
			return true;
		}
		return hasAuthority(authToken, roleToGrantedAuthority(role));
	}
	
	/**
	 * Returns true if the given user is an admin.
	 * @param authToken current users' auth token (may be null if the user is anonymous)
	 * @return true if given auth token belongs to an admin; false if not
	 */
	public static boolean isAdmin(@Nullable AuthenticationToken authToken) {
		if (authToken == null) {
			return false;
		}
		return authToken.getAuthorities()
				.stream()
				.anyMatch(a -> a.equals(grantedAuthority(SecurityConfiguration.ADMIN_ACCESS_ROLE)));
	}
	
	/**
	 * Check if the given user is anonymous.
	 * @param authToken current users' authentication token
	 * @return true if token is null; false othwerise
	 */
	public static boolean isAnonymous(@Nullable AuthenticationToken authToken) {
		return authToken == null;
	}
	
}
