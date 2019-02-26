package at.sparklingscience.urbantrees.security;

import java.util.Collection;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.WebExpressionVoter;

import at.sparklingscience.urbantrees.SecurityConfiguration;
import at.sparklingscience.urbantrees.controller.util.ControllerUtil;
import at.sparklingscience.urbantrees.security.jwt.AuthenticationToken;

/**
 * Decides if access should be granted to the admin.
 * Admins are only allowed to access admin-functionality the first 30 minutes after token creation.
 * 
 * @author Laurenz Fiala
 * @since 2019/02/06
 */
public class AdminAccessDecisionVoter extends WebExpressionVoter {

	@Override
	public int vote(Authentication authentication, FilterInvocation fi, Collection<ConfigAttribute> attributes) {
		
		String expressions = "";
		for (ConfigAttribute attr : attributes) {
			expressions += attr.toString();
		}
		
		final boolean resourceNeedsAdminRole = expressions.indexOf(SecurityUtil.role(SecurityConfiguration.ADMIN_ACCESS_ROLE)) != -1;
		
		if (!(authentication instanceof AuthenticationToken) || !resourceNeedsAdminRole) {
			return ACCESS_ABSTAIN;
		}
		
		AuthenticationToken authToken = ControllerUtil.getAuthToken(authentication);
		
		if (authToken.getTokenCreationDate().getTime() < System.currentTimeMillis() - SecurityConfiguration.ADMIN_MAX_TOKEN_AGE) {
			return ACCESS_DENIED;
		}
		return ACCESS_ABSTAIN;
		
	}

}
