package at.sparklingscience.urbantrees.service;

import java.util.Collection;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import at.sparklingscience.urbantrees.domain.Role;
import at.sparklingscience.urbantrees.domain.UserContent;
import at.sparklingscience.urbantrees.domain.UserContentMetadata;
import at.sparklingscience.urbantrees.domain.UserIdentity;
import at.sparklingscience.urbantrees.domain.UserPermission;
import at.sparklingscience.urbantrees.exception.BadRequestException;
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
	 * Check that the given content is enabled.
	 * @param contentId content id
	 * @throws BadRequestException if the content is disabled.
	 */
	private void assertContentEnabled(String contentId) throws BadRequestException {
		
		if (this.contentMapper.isContentEnabled(contentId)) {
			throw new BadRequestException("Not allowed");
		}
	
	}
	
	/**
	 * Check whether the given user is allowed to view given user content.
	 * Guarantees to throw an {@link UnauthorizedException} if this is not the case.
	 * @param auth current users' auth token.
	 * @param contentId content to check permissions for.
	 * @throws UnauthorizedException if content may not be viewed by the given user.
	 */
	private void assertViewPermission(AuthenticationToken auth, String contentId) throws UnauthorizedException {

		this.assertContentEnabled(contentId);
		
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

		this.assertContentEnabled(contentId);
		
		Collection<? extends GrantedAuthority> grantedAuthorities = null;
		if (auth != null) {
			grantedAuthorities = auth.getAuthorities();
		}
		
		if (!this.contentMapper.canViewContent(contentId, grantedAuthorities)) {
			throw new UnauthorizedException("User is not allowed to edit this content", auth);
		}
		
	}
	
	/**
	 * Check whether the user is allowed to approve contentId edited by user with editUserId (or null if anonymous).
	 * 
	 * @param auth current users' auth token
	 * @param editUserId the used who edited the content which needs to be approved
	 * @param contentId the content id of the content to be approved
	 * @throws UnauthorizedException if content may not be approved by this user
	 */
	private void assertApprovalPermission(@NotNull AuthenticationToken auth,
										  @Nullable Integer editUserId,
										  @NotNull String contentId) throws UnauthorizedException {
		
		this.assertContentEnabled(contentId);
		
		Collection<? extends GrantedAuthority> grantedAuthorities = null;
		if (auth != null) {
			grantedAuthorities = auth.getAuthorities();
		}
		
		List<Role> editRoles = null;
		if (editUserId != null) {
			editRoles = this.authService.getUserRoles(editUserId);
		}
		
		if (!this.contentMapper.canApproveContent(contentId, editRoles, grantedAuthorities)) {
			throw new UnauthorizedException("User is not allowed to view this content", auth);
		}
		
	}
	
	/**
	 * Approve content by given uid with given user.
	 * @param authToken current users' auth token
	 * @param contentUid unique id 
	 */
	public void approveContent(AuthenticationToken authToken, long contentUid) {
		
		UserContentMetadata metadata = this.contentMapper.findContentMetadataById(contentUid);
		if (metadata == null) {
			throw new BadRequestException("Unknown content");
		}
		
		Integer editUserId = null;
		UserIdentity editUser = metadata.getUser();
		if (editUser != null) {
			editUserId = editUser.getId();
		}
		
		this.assertApprovalPermission(authToken, editUserId, metadata.getContentId());
		if (this.contentMapper.approveContentById(contentUid, authToken.getId()) == 0) {
			throw new BadRequestException("Content is already approved.");
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
