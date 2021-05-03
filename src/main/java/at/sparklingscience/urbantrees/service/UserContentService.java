package at.sparklingscience.urbantrees.service;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.sparklingscience.urbantrees.cms.CmsContent;
import at.sparklingscience.urbantrees.cms.CmsContentViews;
import at.sparklingscience.urbantrees.cms.CmsElement;
import at.sparklingscience.urbantrees.domain.Role;
import at.sparklingscience.urbantrees.domain.UserContent;
import at.sparklingscience.urbantrees.domain.UserContentFile;
import at.sparklingscience.urbantrees.domain.UserContentLanguage;
import at.sparklingscience.urbantrees.domain.UserContentMetadata;
import at.sparklingscience.urbantrees.domain.UserContentSaveAmount;
import at.sparklingscience.urbantrees.domain.UserIdentity;
import at.sparklingscience.urbantrees.domain.UserPermission;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.ClientError;
import at.sparklingscience.urbantrees.exception.TooManyRequestsException;
import at.sparklingscience.urbantrees.exception.UnauthorizedException;
import at.sparklingscience.urbantrees.exception.ValidationException;
import at.sparklingscience.urbantrees.mapper.UserContentMapper;
import at.sparklingscience.urbantrees.security.SecurityUtil;
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

	@Autowired
	private ObjectMapper jsonObjectMapper;
	
	@Value("${at.sparklingscience.urbantrees.userContent.maxSavesPerUserPerDay}")
	private int maxSavesPerUserPerDay;
	
	/**
	 * Check that the given content is enabled.
	 * If a contentId is disabled, it means that it must not be viewed, edited or approved.
	 * @param contentId content id
	 * @return true if the content is enabled; false otherwise.
	 */
	private boolean isContentEnabled(String contentId) throws BadRequestException {
		
		return this.contentMapper.isContentEnabled(contentId);
	
	}
	
	/**
	 * Check that the given content is enabled.
	 * @param contentId content id
	 * @throws BadRequestException if the content is disabled.
	 */
	private void assertContentEnabled(String contentId) throws BadRequestException {
		
		if (!this.contentMapper.isContentEnabled(contentId)) {
			throw new BadRequestException("Not allowed");
		}
	
	}
	
	/**
	 * Check whether the given user is allowed to view given user content.
	 * @param auth current users' auth token.
	 * @param contentId content to check permissions for.
	 * @return true if the given user is allowed to view; false otherwise.
	 */
	private boolean hasViewPermission(AuthenticationToken auth, String contentId) throws UnauthorizedException {

		this.assertContentEnabled(contentId);
		
		Collection<? extends GrantedAuthority> grantedAuthorities = null;
		if (auth != null) {
			grantedAuthorities = auth.getAuthorities();
		}
		
		return this.contentMapper.canViewContent(contentId, grantedAuthorities);
		
	}
	
	/**
	 * Check whether the given user is allowed to view given user content.
	 * Guarantees to throw an {@link UnauthorizedException} if this is not the case.
	 * @param auth current users' auth token.
	 * @param contentId content to check permissions for.
	 * @throws UnauthorizedException if content may not be viewed by the given user.
	 */
	private void assertViewPermission(AuthenticationToken auth, String contentId) throws UnauthorizedException {
		
		if (!this.hasViewPermission(auth, contentId)) {
			throw new UnauthorizedException("User is not allowed to view this content", auth);
		}
		
	}
	
	/**
	 * Check whether the given user is allowed to edit given user content.
	 * @param auth current users' auth token.
	 * @param contentId content to check permissions for.
	 * @return true if the given user is allowed to edit; false otherwise.
	 */
	private boolean hasEditPermission(AuthenticationToken auth, String contentId) throws UnauthorizedException {

		this.assertContentEnabled(contentId);
		
		Collection<? extends GrantedAuthority> grantedAuthorities = null;
		if (auth != null) {
			grantedAuthorities = auth.getAuthorities();
		}
		
		return this.contentMapper.canViewContent(contentId, grantedAuthorities);
		
	}
	
	/**
	 * Check whether the given user is allowed to edit given user content.
	 * Guarantees to throw an {@link UnauthorizedException} if this is not the case.
	 * @param auth current users' auth token.
	 * @param contentId content to check permissions for.
	 * @throws UnauthorizedException if content may not be edited by the given user.
	 */
	private void assertEditPermission(AuthenticationToken auth, String contentId) throws UnauthorizedException {
		
		if (!this.hasEditPermission(auth, contentId)) {
			throw new UnauthorizedException("User is not allowed to edit this content", auth);
		}
		
	}
	
	/**
	 * Check whether the user is allowed to approve contentId edited by user with editUserId (or null if anonymous).
	 * @param auth current users' auth token
	 * @param editUserId the used who edited the content which needs to be approved
	 * @param contentId the content id of the content to be approved
	 * @return true if the given user is allowed to approve; false otherwise.
	 */
	private boolean hasApprovalPermission(@NotNull AuthenticationToken auth,
									   	  @Nullable Integer editUserId,
									   	  @NotNull String contentId) {

		this.assertContentEnabled(contentId);
		
		Collection<? extends GrantedAuthority> grantedAuthorities = null;
		if (auth != null) {
			grantedAuthorities = auth.getAuthorities();
		}
		
		List<Role> editRoles = null;
		if (editUserId != null) {
			editRoles = this.authService.getUserRoles(editUserId);
		}
		
		return this.contentMapper.canApproveContent(contentId, editRoles, grantedAuthorities);
	
	}
	
	/**
	 * Check whether the user is allowed to approve contentId edited by user with editUserId (or null if anonymous).
	 * Guarantees to throw an {@link UnauthorizedException} if this is not the case.
	 * @param auth current users' auth token
	 * @param editUserId the used who edited the content which needs to be approved
	 * @param contentId the content id of the content to be approved
	 * @throws UnauthorizedException if content may not be approved by this user
	 */
	private void assertApprovalPermission(@NotNull AuthenticationToken auth,
										  @Nullable Integer editUserId,
										  @NotNull String contentId) throws UnauthorizedException {
		
		if (!this.hasApprovalPermission(auth, editUserId, contentId)) {
			throw new UnauthorizedException("User is not allowed to view this content", auth);
		}
		
	}
	
	/**
	 * Approve content by given uid with given user.
	 * @param authToken current users' auth token
	 * @param contentUid unique id
	 * @throws BadRequestException if content id is unknown or is already approved.
	 */
	public void approveContent(AuthenticationToken authToken, long contentUid) throws BadRequestException {
		
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
	
	/**
	 * Check whether the user is allowed to save content.
	 * If the user exceeded their daily quota (see config: at.sparklingscience.urbantrees.userContent.maxSavesPerUserPerDay)
	 * a {@link TooManyRequestsException} is thrown.
	 * @param authToken users' auth token
	 * @throws TooManyRequestsException if the user may not insert a new content to the DB (exceeded their quota)
	 */
	private void throttleContentSaving(AuthenticationToken authToken) throws TooManyRequestsException {
		
		Integer userIdOrNull = null;
		if (authToken != null) {
			userIdOrNull = authToken.getId();
			
			if (SecurityUtil.isAdmin(authToken)) {
				return;
			}
		}
		
		UserContentSaveAmount saveAmount = this.contentMapper.findSavedContentAmountForUserId(userIdOrNull);
		if (saveAmount.getAmount() > maxSavesPerUserPerDay) {
			throw new TooManyRequestsException("You may not save more than " + maxSavesPerUserPerDay + " content entries per day.",
											   saveAmount.getMinSaveDate().toInstant().plus(1, ChronoUnit.DAYS));
		}
		
	}
	
	/**
	 * Get all published/approved contents for the given content id.
	 * @param authToken current user auth token
	 * @param contentId find content entries of this conent id
	 * @param contentLangId find content entries of this language
	 * @param substituteUserDrafts true to substitute content for the users' draft, if available;
	 * 							   false to only return approved content
	 * @return list of all published content entries.
	 */
	public @NonNull List<UserContent> getContent(@Nullable AuthenticationToken authToken,
												 @NonNull String contentId,
												 @NonNull String contentLangId,
												 boolean substituteUserDrafts) {
		
		LOGGER.debug("getContent - contentId: {}", contentId);
		this.assertViewPermission(authToken, contentId);
		
		List<UserContent> content = this.contentMapper.findAllContent(
			contentId,
			UserContentLanguage.fromId(contentLangId),
			substituteUserDrafts
		);
		
		content.parallelStream().forEach(c -> {
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
	 * Save the given user content. Validates given user content so it can't overwrite
	 * other user's drafts or published content.
	 * @param authToken current user's authentication token.
	 * @param contentId content id
	 * @param contentOrder content sub-order
	 * @param contentLanguage order of content to save
	 * @param isDraft whether the given content should be saved as draft or published
	 * @param content content to insert/update depending on it's content (draft, id, etc.)
	 * 				  Note: this content must have already been validated
	 * @return inserted/updated user content entry
	 * @throws BadRequestException either when user is not allowed to edit the given content,
	 * 							   or when the instant-approval of a previous draft fails.
	 */
	@Transactional
	public @NonNull UserContent saveContent(@NotNull AuthenticationToken authToken,
											@NotNull String contentId,
											int contentOrder,
											@NotNull String contentLanguage,
											boolean isDraft,
											@NonNull CmsContent cmsContent) throws BadRequestException {
		
		this.assertEditPermission(authToken, contentId);
		
		UserIdentity user = UserIdentity.fromAuthToken(authToken);
		UserContent content;
		try {
			content = CmsContent.toUserContent(
					cmsContent,
					contentId,
					contentOrder,
					contentLanguage,
					isDraft,
					user,
					this.jsonObjectMapper.writerWithView(CmsContentViews.Persist.class)
					);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Can't serialize CmsContent back to string", e);
		}

		// opportunistic locking
		this.assertLatestContentBase(content);
		
		// instant-approval if user has permission
		if (!content.isDraft() && this.hasApprovalPermission(authToken, user.getId(), contentId)) {
			content.setApproveDate(new Date());
			content.setApproveUser(user);
		} else {
			content.setApproveDate(null);
			content.setApproveUser(null);
		}

		UserContentMetadata userDraft = this.contentMapper.findContentUserDraft(
				content.getContentId(),
				content.getContentOrder(),
				content.getContentLanguage(),
				user.getId()
		);
		
		if (content.isDraft()) { 					// save draft
			if (userDraft == null) { 				// -> save new draft
				this.throttleContentSaving(authToken);
				content.setDraft(true);
				this.contentMapper.insertContent(content);
			} else { 								// -> update existing draft
				this.contentMapper.updateContentDraft(userDraft.getId(), content);
			}
		} else { 									// publish
			if (userDraft == null) { 				// -> publish right away (without existing draft)
				this.throttleContentSaving(authToken);
				this.contentMapper.insertContent(content);
			} else { 								// -> was saved draft before, update is_draft = false
				this.contentMapper.updateContentDraft(userDraft.getId(), content);
				this.contentMapper.updateContentPublish(userDraft.getId());
				if (content.getApproveDate() != null) {
					this.approveContent(authToken, userDraft.getId());			
				}
			}
		}
		
		return content;
		
	}
	
	/**
	 * Get the given content file by id if the given current user is allowed to view it.
	 * There are three cases in which a user is allowed to view a file:
	 * - the user is the same user who uploaded the file
	 * - the user has viewing permission, the file is active and the associated content is approved
	 * - the user has viewing permission, the file is inactive and the user has approval permission
	 * @param authToken current user auth token (pass null if user is anonymous)
	 * @param contentId content id of the associated content
	 * @param fileId id of the specific file to get
	 * @return {@link UserContentFile}
	 * @throws BadRequestException see {@link #assertViewPermission(AuthenticationToken, String)}
	 * @throws UnauthorizedException if given user may not view the file
	 */
	public @NonNull UserContentFile getContentFile(@Nullable AuthenticationToken authToken,
							   					   @NonNull String contentId,
							   					   long fileId) throws BadRequestException, UnauthorizedException {
		
		this.assertViewPermission(authToken, contentId);
		
		UserContentFile file = this.contentMapper.findContentFile(fileId, contentId);
		UserContentMetadata metadata = this.contentMapper.findContentMetadataById(file.getActivateContentUid());
		UserIdentity editUser = metadata.getUser();
		
		// same user
		if (authToken != null && file.getUserId() == authToken.getId()) {
			return file;
			
		// active file and approved content
		} else if (file.isActive() && file.getActivateContentUid() != null && !metadata.isDraft() && metadata.getApproveDate() != null) {
			return file;
			
		// current user is approver
		} else if (this.hasApprovalPermission(authToken, editUser == null ? null : editUser.getId(), contentId)) {
			return file;
		}
		
		throw new UnauthorizedException("Unauthorized access to file", authToken);
		
	}
	
	/**
	 * Get CMS content the given user has edited previously.
	 * Only returns one entry per content id.
	 * Also checks if the current user is allowed to view the content history.
	 * 
	 * @param authToken current users' auth token
	 * @param userId id of user to see
	 * @param contentIdPrefix filter by content id prefix
	 * @return list of the 10 newest {@link UserContentMetadata}. first item is the users' latest edited content.
	 * @throws UnauthorizedException if current user does not have permission to view given users content history
	 */
	public @NonNull List<UserContentMetadata> getContentUserHistory(@Nullable AuthenticationToken authToken,
														   			int userId,
														   			@Nullable String contentIdPrefix) throws UnauthorizedException {
		
		if (!this.authService.hasUserPermission(userId, authToken.getId(), UserPermission.CONTENT_HISTORY)) {
			throw new UnauthorizedException("You are not allowed to request the given users' content history.", authToken);
		}
		
		return this.contentMapper.findContentUserHistory(userId, contentIdPrefix, 10);
		
	}
	
	/**
	 * Check if the given content has the latest history_id, meaning
	 * it is based off of the newest published content.
	 * @param userContent the content to check
	 * @throws BadRequestException if the history id is outdated.
	 */
	private void assertLatestContentBase(UserContent userContent) throws BadRequestException {
		
		UserContentMetadata newestPublishedContent = this.contentMapper.findContentMetadata(
				userContent.getContentId(),
				userContent.getContentOrder(),
				userContent.getContentLanguage()
		);
		
		if (newestPublishedContent != null && newestPublishedContent.getId() != userContent.getHistoryId()) {
			throw new BadRequestException("Your content is based on outdated content.", ClientError.CONTENT_BASE_OUTDATED);
		}
		
	}
	
	/**
	 * Validate the given CMS content and all its elements.
	 * If there are any errors, {@link ValidationException}
	 * is thrown.
	 * @param content cms content received from the frontend
	 * @param errors errors object from the controller
	 * @throws ValidationException if any errors are reported by the
	 * 							   {@link CmsElement#validate(Errors)} methods
	 */
	public void assertValid(CmsContent content, Errors errors) throws ValidationException {
		
		errors.pushNestedPath("content");
		content.validate(errors);
		errors.popNestedPath();
		
		if (errors.hasErrors()) {
			throw new ValidationException("CMS content validation failed.", errors);
		}
		
	}

}
