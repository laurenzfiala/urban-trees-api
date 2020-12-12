package at.sparklingscience.urbantrees.service;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import at.sparklingscience.urbantrees.domain.UserContent;
import at.sparklingscience.urbantrees.domain.UserContentMetadata;
import at.sparklingscience.urbantrees.domain.UserIdentity;
import at.sparklingscience.urbantrees.domain.UserPermission;
import at.sparklingscience.urbantrees.exception.UnauthorizedException;
import at.sparklingscience.urbantrees.mapper.UserContentMapper;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;

/**
 * Service for user-content actions.
 * 
 * @author Laurenz Fiala
 * @since 2019/03/13
 */
@Service
public class UserContentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserContentService.class);
	
	@Autowired
    private AuthenticationService authService;
	
	@Autowired
    private UserContentMapper contentMapper;
	
	/**
	 * Check whether the given user is allowed to view given user content.
	 * Guarantees to throw an {@link UnauthorizedException} if this is not the case.
	 * @param auth current users' auth token.
	 * @param contentId content to check permissions for.
	 * @throws UnauthorizedException if content may not be viewed by the given user.
	 */
	private void assertViewPermission(AuthenticationToken auth, String contentId) throws UnauthorizedException {
		
		Collection<? extends GrantedAuthority> grantedAuthorities = null;
		if (auth != null) {
			grantedAuthorities = auth.getAuthorities();
		}
		
		if (!this.contentMapper.canViewContent(contentId, grantedAuthorities)) {
			throw new UnauthorizedException("User is not allowed to view this content", auth);
		}
		
	}
	
	/**
	 * Check whether the given user is allowed to edit given user content.
	 * Guarantees to throw an {@link UnauthorizedException} if this is not the case.
	 * @param auth current users' auth token.
	 * @param contentId content to check permissions for.
	 * @throws UnauthorizedException if content may not be edited by the given user.
	 */
	private void assertEditPermission(AuthenticationToken auth, String contentId) throws UnauthorizedException {
		
		Collection<? extends GrantedAuthority> grantedAuthorities = null;
		if (auth != null) {
			grantedAuthorities = auth.getAuthorities();
		}
		
		if (!this.contentMapper.canViewContent(contentId, grantedAuthorities)) {
			throw new UnauthorizedException("User is not allowed to edit this content", auth);
		}
		
	}
	
	public List<UserContent> getContent(AuthenticationToken authToken, String contentId) {
		
		LOGGER.debug("getContent - contentId: {}", contentId);
		this.assertViewPermission(authToken, contentId);
		
		List<UserContent> content = this.contentMapper.findAllContent(contentId);
		
		content.stream().forEach(c -> {
			UserIdentity grantingUser = c.getUser();
			
			if (authToken == null || (
					grantingUser != null &&
					!this.authService.hasUserPermission(grantingUser.getId(), authToken, UserPermission.DISPLAY_USERNAME)
				)) {
				c.setUser(null);
				c.setApproveUser(null);					
			}
		});
		
		return content;
		
	}
	
	/**
	 * Get CMS content the given user has edited previously.
	 * Only returns one entry per content id.
	 * Also checks if the current user is allowed to view the content history.
	 * @param authToken current users' auth token
	 * @param userId id of user to see
	 * @param contentIdPrefix filter by content id prefix
	 */
	public List<UserContentMetadata> getContentUserHistory(AuthenticationToken authToken, int userId, String contentIdPrefix) {
		
		if (!this.authService.hasUserPermission(userId, authToken.getId(), UserPermission.CONTENT_HISTORY)) {
			throw new UnauthorizedException("You are not allowed to request the given users' content history.", authToken);
		}
		
		return this.contentMapper.findContentUserHistory(userId, contentIdPrefix, 10);
		
	}

}
