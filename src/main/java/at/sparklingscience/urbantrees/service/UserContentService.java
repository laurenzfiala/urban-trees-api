package at.sparklingscience.urbantrees.service;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.sparklingscience.urbantrees.cms.CmsContent;
import at.sparklingscience.urbantrees.cms.CmsContentViews;
import at.sparklingscience.urbantrees.cms.CmsElement;
import at.sparklingscience.urbantrees.cms.UserContentConfiguration;
import at.sparklingscience.urbantrees.cms.action.UserContentActions;
import at.sparklingscience.urbantrees.cms.component.FileComponent;
import at.sparklingscience.urbantrees.cms.validation.UserContentValidator;
import at.sparklingscience.urbantrees.domain.UserContent;
import at.sparklingscience.urbantrees.domain.UserContentAccess;
import at.sparklingscience.urbantrees.domain.UserContentLanguage;
import at.sparklingscience.urbantrees.domain.UserContentMetadata;
import at.sparklingscience.urbantrees.domain.UserContentStatus;
import at.sparklingscience.urbantrees.domain.UserIdentity;
import at.sparklingscience.urbantrees.domain.UserPermission;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.ClientError;
import at.sparklingscience.urbantrees.exception.InternalException;
import at.sparklingscience.urbantrees.exception.TooManyRequestsException;
import at.sparklingscience.urbantrees.exception.UnauthorizedException;
import at.sparklingscience.urbantrees.exception.ValidationException;
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
    private UserContentValidator contentValidator;
	
	@Autowired
    private UserContentActions contentActions;
	
	@Autowired
    private AuthenticationService authService;
	
	@Autowired
    private UserContentAccessService accessService;
	
	@Autowired
    private UserContentFileService fileService;
	
	@Autowired
    private UserContentMapper contentMapper;

	@Autowired
	@Qualifier("jsonCmsObjectMapper")
	private ObjectMapper jsonObjectMapper;
	
	/**
	 * Get all published/approved contents for the given content id.
	 * @param authToken current user auth token
	 * @param contentPath find content entries of this conent id
	 * @param contentLangId find content entries of this language
	 * @param substituteUserDrafts true to substitute content for the users' draft, if available;
	 * 							   false to only return approved content TODO check impl
	 * @return list of all published content entries.
	 */
	public @NonNull List<UserContent> getContent(@Nullable AuthenticationToken authToken,
												 @NonNull String contentPath,
												 @NonNull String contentLangId,
												 boolean substituteUserDrafts) {
		
		LOGGER.debug("getContent - contentPath: {}", contentPath);
		this.accessService.assertViewPermission(authToken, contentPath);
		
		final UserIdentity user = UserIdentity.fromAuthToken(authToken);
		List<UserContent> content = this.contentMapper.findContent(
			contentPath,
			UserContentLanguage.fromId(contentLangId),
			user == null ? null : user.getId(),
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
	 * @param authToken current user's authentication token
	 * @param contentPath content path
	 * @param contentLanguage order of content to save
	 * @param isDraft whether the given content should be saved as draft or published
	 * @param content content to insert/update depending on it's content (draft, id, etc.)
	 * 				  Note: this content must have already been validated
	 * @return inserted/updated user content entry
	 * @throws BadRequestException either when user is not allowed to edit the given content,
	 * 							   or when the instant-approval of a previous draft fails,
	 * 							   or when the CMS content contains any invalid file refs.
	 * @throws TooManyRequestsException if the user may not insert a new content to the DB (exceeded their quota)
	 */
	@Transactional
	public @NonNull UserContent saveContent(@NotNull AuthenticationToken authToken,
											@NotNull String contentPath,
											@NotNull String contentLanguage,
											boolean isDraft,
											@NonNull CmsContent cmsContent)
								throws BadRequestException, TooManyRequestsException {
		
		this.accessService.assertEditPermission(authToken, contentPath);
		this.accessService.throttleContentSaving(authToken);
		this.contentValidator.check(contentPath, authToken, cmsContent);
		cmsContent.sanitize();
		
		UserIdentity user = UserIdentity.fromAuthToken(authToken);
		UserContent content;
		try {
			content = CmsContent.toUserContent(
					cmsContent,
					contentPath,
					contentLanguage,
					isDraft ? UserContentStatus.DRAFT : UserContentStatus.DRAFT_AWAITING_APPROVAL,
					user,
					this.jsonObjectMapper.writerWithView(CmsContentViews.Persist.class)
					);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Can't serialize CmsContent back to string", e);
		}
		
		boolean doUpdate = false;
		
		// instant-approval
		if (content.getStatus() == UserContentStatus.DRAFT_AWAITING_APPROVAL &&
			this.accessService.hasApprovalPermission(authToken, user, contentPath)) { 
			content.setStatus(UserContentStatus.APPROVED);
			content.setApproveDate(new Date());
			content.setApproveUser(user);
		}
		
		// check if draft exists
		UserContentMetadata userDraft = null;
		userDraft = this.contentMapper.findContentUserDraft(
				content.getContentPath(),
				content.getHistoryId(),
				content.getPreviousId(),
				content.getNextId(),
				content.getContentLanguage(),
				user.getId()
				);
		if (userDraft != null) {
			content.setId(userDraft.getId());
			content.setHistoryId(userDraft.getHistoryId()); // TODO what happens when draft is outdated?
			doUpdate = true;
		}
		
		// validation/update of prev/next id
		if (content.getHistoryId() != null) {
			this.assertNewestContentBase(content, authToken);			
		}
		this.updatePrevNextIds(content, authToken);
		
		UserContent baseContent = null;
		List<UserContentAccess> access = this.accessService.getContentAccess(contentPath);
		boolean keepHistory = this.accessService.shouldKeepHistory(access);
		
		if (content.getHistoryId() != null) {
			baseContent = this.contentMapper.findContentById(content.getHistoryId());
			if (content.getStatus() == UserContentStatus.APPROVED && !keepHistory) { // overwrite existing entry if keepHistory = false
				content.setId(baseContent.getId());
				content.setHistoryId(baseContent.getHistoryId());
				if (userDraft != null) {
					this.contentMapper.deleteContentUserDraft(userDraft.getId());
				}
				doUpdate = true;
			} else if (baseContent.getStatus().isTransient()) { // base content status must be permanent
				throw new BadRequestException("Can't base new content on content with transient status.");
			}
		}
		
		try {
			if (doUpdate) {
				if (this.contentMapper.updateContent(content) == 0) {
					throw new InternalException("No content was inserted/updated");
				}
			} else {
				this.contentMapper.registerContent(content.getContentPath(), access);
				this.contentMapper.insertContent(content);
			}
		} catch (DataIntegrityViolationException e) {
			LOGGER.error("Invalid content metadata", e);
			throw new BadRequestException("Invalid content metadata");
		}
		
		if (content.getStatus() == UserContentStatus.APPROVED) {
			this.contentMapper.stitchContent(content, content.getId(), content.getId());
			this.fileService.publishContentUpdateFiles(content, cmsContent, user);
		}
		
		// post-save actions
		this.fileService.cleanUpFiles(content, cmsContent, baseContent, keepHistory);
		this.contentActions.doActions(contentPath, content, authToken);
		
		return content;
		
	}
	
	/**
	 * TODO
	 * @param authToken
	 * @param contentUid
	 */
	public void deleteContent(@NotNull AuthenticationToken authToken,
			  long contentUid) {
		this.deleteContent(authToken, contentUid, false);
	}
	
	/**
	 * TODO
	 * Set the state of the content with given UID to AWAITING_DELETION, or,
	 * if the current user has approval permission, DELETED.
	 * If the previous and next content entries are still pointing to that
	 * entry, update them to directly reference each other.
	 * @param authToken current users' authentication
	 * @param contentUid content UID to delete
	 * @return the deleted user content
	 */
	public void deleteContent(@NotNull AuthenticationToken authToken,
							  long contentUid,
							  boolean draftOnly) {

		UserIdentity user = UserIdentity.fromAuthToken(authToken);
		UserContent content = this.contentMapper.findContentById(contentUid);
		if (content == null) {
			throw new BadRequestException("No content with given UID found.");
		}
		
		UserContentStatus updateStatus = null;
		if (content.getStatus() == UserContentStatus.DRAFT && content.getUser().getId() == authToken.getId()) {
			LOGGER.debug("Fully deleting content {contentUid} with status DRAFT by user {userId}.", contentUid, authToken.getId());
			this.contentMapper.deleteContent(contentUid);
			return;
		}
		if (draftOnly) {
			LOGGER.debug("draftOnly is true and content is not DRAFT; skipping deletion.");
			return;
		}
		if (this.accessService.hasEditPermission(authToken, content.getContentPath())) {
			if (content.getStatus() == UserContentStatus.APPROVED) {
				updateStatus = UserContentStatus.APPROVED_AWAITING_DELETION;
			} else if (content.getStatus() == UserContentStatus.DRAFT_AWAITING_APPROVAL) {
				updateStatus = UserContentStatus.DRAFT_AWAITING_DELETION;
			}
		}
		if (this.accessService.hasApprovalPermission(authToken, content.getUser(), content.getContentPath())) {
			updateStatus = UserContentStatus.DELETED;
		}
		if (updateStatus == null) {
			throw new BadRequestException("Content has wrong status to be deleted.");
		}
		
		this.contentMapper.updateContentStatus(contentUid, user.getId(), updateStatus);
		this.deleteContent(user, content);
			
	}
	
	/**
	 * TODO doc
	 * does cleanup and actual deletion/stitching only; method above also sets status
	 * @param user
	 * @param content
	 */
	public void deleteContent(@NotNull UserIdentity user,
			  				  @NotNull UserContent content) {

		this.contentMapper.stitchContent(content, content.getNextId(), content.getPreviousId());
		
		final boolean keepHistory = this.accessService.shouldKeepHistory(content.getContentPath());
		
		try {
			if (keepHistory) {
				UserContent newContent = this.contentMapper.findContentForHistoryId(content.getHistoryId());
				if (newContent == null) return;
				
				CmsContent newCmsContent = this.jsonObjectMapper.readValue(newContent.getContent(), CmsContent.class);
				this.fileService.deactivateFiles(content.getId(), List.of(), FileComponent.findUidsForContent(newCmsContent));
			} else {
				CmsContent deletedCmsContent = this.jsonObjectMapper.readValue(content.getContent(), CmsContent.class);
				this.contentMapper.deleteContent(content.getId());
				this.fileService.deleteFiles(content.getId(), FileComponent.findUidsForContent(deletedCmsContent), List.of());
				
				if (this.contentMapper.countContentRegistryReferencesForPath(content.getContentPath()) == 0) {
					this.contentMapper.deregisterContent(content.getContentPath());
				}
			}
		} catch (JsonProcessingException e) {
			throw new RuntimeException(
				"Can't delete files for predecessor (ID: "
				+ content.getHistoryId() + ") because it is invalid."
				);
		}
	
	}
	
	/**
	 * TODO
	 * 
	 * Get CMS content the given user has edited previously.
	 * Only returns one entry per content id.
	 * Also checks if the current user is allowed to view the content history.
	 * 
	 * @param authToken current users' auth token
	 * @param userId id of user to see
	 * @param contentPathExpression filter by content path expression
	 * 								(see {@link UserContentConfiguration#CONTENT_PATH_EXP_VALIDATION_REGEX}
	 * @return list of the 10 newest {@link UserContentMetadata}. first item is the users' latest edited content.
	 * @throws UnauthorizedException if current user does not have permission to view given users content history
	 */
	public @NonNull List<UserContentMetadata> getContentUserHistory(@Nullable AuthenticationToken authToken,
														   			int userId,
														   			@Nullable String contentPathExpression) throws UnauthorizedException {
		
		if (!this.authService.hasUserPermission(userId, authToken.getId(), UserPermission.CONTENT_HISTORY)) {
			throw new UnauthorizedException("You are not allowed to request the given users' content history.", authToken);
		}
		
		String pathExp = null;
		if (contentPathExpression != null) {
			this.contentValidator.checkPathExp(contentPathExpression);
			
			Matcher pathMatcher = Pattern.compile("(?:\\/((?:[a-zA-Z0-9]+)|\\*{1,2}))").matcher(contentPathExpression);
			StringBuffer pathExpBuffer = new StringBuffer();
			while (pathMatcher.find()) {
				switch (pathMatcher.group(1)) {
				case "**":
					pathMatcher.appendReplacement(pathExpBuffer, "\\/.+");
					break;
				case "*":
					pathMatcher.appendReplacement(pathExpBuffer, "\\/[^\\\\/]+");
					break;
				}
			}
			pathExp = pathExpBuffer.toString();
		}
		
		return this.contentMapper.findContentUserHistory(userId, pathExp, 10);
		
	}
	
	/**
	 * Check if the given content has the latest history_id, meaning
	 * it is based off of the newest published content.
	 * @param userContent the content to check
	 * @param authToken current users' authentication
	 * @throws BadRequestException if the history id is outdated.
	 */
	private void assertNewestContentBase(UserContent userContent,
										 AuthenticationToken authToken) throws BadRequestException {
		
		List<Long> contentIdList = this.contentMapper.findContentIdListForPath(
				userContent.getContentPath(),
				userContent.getContentLanguage(),
				authToken.getId(),
				false
		);
		
		int baseIndex = contentIdList.indexOf(userContent.getHistoryId());
		if (baseIndex == -1) {
			throw new BadRequestException("Your content is based on outdated content.", ClientError.CONTENT_BASE_OUTDATED);
		}
		
	}
	
	/**
	 * Update the given user content to reference the most recent adjacent
	 * content uids.
	 * @param userContent the content to check
	 * @param authToken current users' authentication
	 */
	private void updatePrevNextIds(UserContent userContent,
								   AuthenticationToken authToken) {
		
		if (userContent.getStatus() == UserContentStatus.DRAFT) {
			return;
		}
		
		List<Long> contentIdList = this.contentMapper.findContentIdListForPath(
				userContent.getContentPath(),
				userContent.getContentLanguage(),
				authToken.getId(),
				false
		);
		
		int baseIndex = contentIdList.indexOf(userContent.getHistoryId());
		int prevIndex = baseIndex - 1;
		int nextIndex = baseIndex + 1;

		if (prevIndex < 0 || prevIndex >= contentIdList.size()) {
			userContent.setPreviousId(null);
		} else {
			userContent.setPreviousId(contentIdList.get(prevIndex));
		}

		if (nextIndex >= contentIdList.size() || nextIndex < 0) {
			userContent.setNextId(null);
		} else {
			userContent.setNextId(contentIdList.get(nextIndex));
		}
		
	}
	
	/**
	 * Check if the given user content references valid predecessor and
	 * successor and throw {@link BadRequestException} if they are invalid.
	 * This method is supposed to be called before insertion of new content
	 * entry (since prev and next id are checked to be adjacent).
	 * @param userContent the content to check
	 * @param authToken current users' authentication
	 * @throws BadRequestException if the history id is outdated, or previous ID
	 * 							   and next ID are not adjacent.
	 */
	private void assertValidPrevNextIds(UserContent userContent,
			   							AuthenticationToken authToken) throws BadRequestException {
		
		boolean ok = true;
		
		List<Long> contentIdList = this.contentMapper.findContentIdListForPath(
				userContent.getContentPath(),
				userContent.getContentLanguage(),
				authToken.getId(),
				userContent.getStatus() == UserContentStatus.DRAFT
		);

		final Long previousId = userContent.getPreviousId();
		final Long nextId = userContent.getNextId();
		
		final int previousIdIndex = contentIdList.indexOf(previousId);
		final int nextIdIndex = contentIdList.indexOf(nextId);
		
		if (previousId == null && nextIdIndex == 0) {
			// first element
		} else if (previousIdIndex == contentIdList.size() - 1 && nextId == null) {
			// last element
		} else if (previousIdIndex + 1 == nextIdIndex) {
			// element is somewhere in-between
		} else if (previousId == null && nextId == null && contentIdList.size() == 1) {
			// only one element exists
		} else {
			ok = false;
		}
		
		UserContentMetadata prevContentMetadata = this.contentMapper.findContentMetadataById(previousId);
		if (prevContentMetadata != null
				&& (!userContent.getContentLanguage().equals(prevContentMetadata.getContentLanguage())
				|| prevContentMetadata.getStatus() != UserContentStatus.APPROVED)) {
			ok = false;
		}
		
		UserContentMetadata nextContentMetadata = this.contentMapper.findContentMetadataById(nextId);
		if (nextContentMetadata != null
				&& (!userContent.getContentLanguage().equals(nextContentMetadata.getContentLanguage())
				|| nextContentMetadata.getStatus() != UserContentStatus.APPROVED)) {
			ok = false;
		}
		
		if (!ok) {
			throw new BadRequestException(
					"Your content references an invalid predecessor or successor.",
					ClientError.CONTENT_INVALID_PREV_NEXT
					);			
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
	 * @throws BadRequestException if the content's content is null
	 */
	public void assertValid(CmsContent content, Errors errors)
			throws ValidationException, BadRequestException {
		
		if (content.getContent() == null) {
			throw new BadRequestException("Content must be given.");
		}
		
		errors.pushNestedPath("content");
		content.validate(errors);
		errors.popNestedPath();
		
		if (errors.hasErrors()) {
			throw new ValidationException("CMS content validation failed.", errors);
		}
		
	}

}
