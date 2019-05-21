package at.sparklingscience.urbantrees.domain;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import at.sparklingscience.urbantrees.security.SecurityUtil;

/**
 * Tags for user content.
 * 
 * @author Laurenz Fiala
 * @since 2019/03/13
 */
public enum UserContentTag {
	
	// example: ADMIN (false, SecurityConfiguration.ROLE_ADMIN),
	NEWS (false),
	TREE (true);
	
	private boolean allowUserAnonymous = false;

	private Collection<GrantedAuthority> authoritiesNeeded = new ArrayList<>();
	
	private UserContentTag(boolean allowUserAnonymous, String... rolesNeeded) {
		this.allowUserAnonymous = allowUserAnonymous;
		for (int i = 0; i < rolesNeeded.length; i++) {
			this.authoritiesNeeded.add(SecurityUtil.grantedAuthority(rolesNeeded[i]));
		}
	}
	
	@Override
	public String toString() {
		return this.name() + ", needs auth? " + !this.allowUserAnonymous + ", roles needed? " + this.authoritiesNeeded;
	}
	
	public static UserContentTag fromString(String val) {
		for (UserContentTag tag : UserContentTag.values()) {
			if (tag.name().equalsIgnoreCase(val)) {
				return tag;
			}
		}
		return null;
	}

	public boolean allowUserAnonymous() {
		return allowUserAnonymous;
	}

	public Collection<GrantedAuthority> getAuthoritiesNeeded() {
		return authoritiesNeeded;
	}

}
