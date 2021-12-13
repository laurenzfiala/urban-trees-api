package at.sparklingscience.urbantrees.service;

import java.nio.file.Path;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.PathMatcher;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.sparklingscience.urbantrees.cms.CmsContent;
import at.sparklingscience.urbantrees.cms.CmsContentViews;
import at.sparklingscience.urbantrees.cms.CmsElement;
import at.sparklingscience.urbantrees.cms.UserContentConfiguration;
import at.sparklingscience.urbantrees.cms.component.FileComponent;
import at.sparklingscience.urbantrees.cms.validation.UserContentValidator;
import at.sparklingscience.urbantrees.domain.ResponseFile;
import at.sparklingscience.urbantrees.domain.Role;
import at.sparklingscience.urbantrees.domain.UserContent;
import at.sparklingscience.urbantrees.domain.UserContentAccess;
import at.sparklingscience.urbantrees.domain.UserContentAccessRole;
import at.sparklingscience.urbantrees.domain.UserContentFile;
import at.sparklingscience.urbantrees.domain.UserContentLanguage;
import at.sparklingscience.urbantrees.domain.UserContentMetadata;
import at.sparklingscience.urbantrees.domain.UserContentSaveAmount;
import at.sparklingscience.urbantrees.domain.UserContentStatus;
import at.sparklingscience.urbantrees.domain.UserIdentity;
import at.sparklingscience.urbantrees.domain.UserPermission;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.ClientError;
import at.sparklingscience.urbantrees.exception.InternalException;
import at.sparklingscience.urbantrees.exception.NotFoundException;
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
	private PathMatcher userContentPathMatcher;
	
	@Autowired
    private UserContentValidator contentValidator;
	
	@Autowired
    private AuthenticationService authService;
	
	@Autowired
    private UserContentMapper contentMapper;

	@Autowired
	@Qualifier("jsonCmsObjectMapper")
	private ObjectMapper jsonObjectMapper;
	
	@Value("${at.sparklingscience.urbantrees.userContent.maxSavesPerUserPerDay}")
	private int maxSavesPerUserPerDay;
	
	/**
	 * Get all applicable content access entries for the given content path.
	 * @param contentPath content path
	 * @return list of content access configs
	 */
	private List<UserContentAccess> getContentAccess(String contentPath) throws BadRequestException {
		
		this.contentValidator.checkPath(contentPath);
		
		String[] pathSegments = contentPath.split(UserContentConfiguration.CONTENT_PATH_SEPARATOR);
		String pathExp = Stream.of(pathSegments)
							   .filter(s -> !s.isEmpty())
							   .map(s -> "(\\/(" + s + "|\\{.+?\\}|\\*{1,2}))?")
							   .collect(Collectors.joining());
		
		List<UserContentAccess> accessCandidates = this.contentMapper.findContentAccess(pathExp);
		if (accessCandidates.size() == 0) {
			throw new BadRequestException("Illegal content path");
		}
		
		return accessCandidates.stream()
							   .filter(c -> this.userContentPathMatcher.match(c.getContentPath(), contentPath))
							   .collect(Collectors.toUnmodifiableList());
		
	}
	
	/**
	 * Check that the given content is enabled.
	 * If any matching content path is disabled, the content must not
	 * be viewed, edited or approved.
	 * @param contentPath content path
	 * @return true if the content is enabled; false otherwise.
	 */
	private boolean isContentEnabled(String contentPath) {
		
		return this.getContentAccess(contentPath)
				.stream()
				.allMatch(a -> a.isEnabled());
	
	}
	
	/**
	 * Check that the given content is enabled.
	 * @param contentPath content path
	 * @throws BadRequestException if the content is disabled.
	 */
	private void assertContentEnabled(String contentPath) throws BadRequestException {
		
		if (!this.isContentEnabled(contentPath)) {
			throw new BadRequestException("Not allowed");
		}
	
	}
	
	/**
	 * Check whether the given user is allowed to view given user content.
	 * @param auth current users' auth token.
	 * @param contentPath content to check permissions for.
	 * @return true if the given user is allowed to view; false otherwise.
	 */
	private boolean hasViewPermission(AuthenticationToken auth, String contentPath) throws UnauthorizedException {

		this.assertContentEnabled(contentPath);
		
		return this.getContentAccess(contentPath).stream().allMatch(a -> {
			if (SecurityUtil.isAdmin(auth)) {
				return true;
			}
			if (SecurityUtil.isAnonymous(auth)) {
				return a.isAnonAllowView();
			}
			Boolean roleAllow = null;
			for (UserContentAccessRole ra : a.getRoleAccess()) {
				if (SecurityUtil.hasRole(auth, ra.getRole())) {
					if (ra.isAllowView() && roleAllow == null) {
						roleAllow = true;
					} else if (!ra.isAllowView()) {
						return false;
					}
				}
			}
			return roleAllow == null ? a.isUserAllowView() : roleAllow;
		});
			
		
	}
	
	/**
	 * Check whether the given user is allowed to view given user content.
	 * Guarantees to throw an {@link UnauthorizedException} if this is not the case.
	 * @param auth current users' auth token.
	 * @param contentPath content to check permissions for.
	 * @throws UnauthorizedException if content may not be viewed by the given user.
	 */
	private void assertViewPermission(AuthenticationToken auth, String contentPath) throws UnauthorizedException {
		
		if (!this.hasViewPermission(auth, contentPath)) {
			throw new UnauthorizedException("User is not allowed to view this content", auth);
		}
		
	}
	
	/**
	 * Check whether the given user is allowed to edit given user content.
	 * @param auth current users' auth token.
	 * @param contentPath content to check permissions for.
	 * @return true if the given user is allowed to edit; false otherwise.
	 */
	private boolean hasEditPermission(AuthenticationToken auth, String contentPath) throws UnauthorizedException {

		this.assertContentEnabled(contentPath);
		
		return this.getContentAccess(contentPath).stream().allMatch(a -> {
			if (SecurityUtil.isAdmin(auth)) {
				return true;
			}
			if (SecurityUtil.isAnonymous(auth)) {
				return a.isAnonAllowEdit();
			}
			Boolean roleAllow = null;
			for (UserContentAccessRole ra : a.getRoleAccess()) {
				if (SecurityUtil.hasRole(auth, ra.getRole())) {
					if (ra.isAllowEdit() && roleAllow == null) {
						roleAllow = true;
					} else if (!ra.isAllowEdit()) {
						return false;
					}
				}
			}
			return roleAllow == null ? a.isUserAllowEdit() : roleAllow;
		});
		
	}
	
	/**
	 * Check whether the given user is allowed to edit given user content.
	 * Guarantees to throw an {@link UnauthorizedException} if this is not the case.
	 * @param auth current users' auth token.
	 * @param contentPath content to check permissions for.
	 * @throws UnauthorizedException if content may not be edited by the given user.
	 */
	private void assertEditPermission(AuthenticationToken auth, String contentPath) throws UnauthorizedException {
		
		if (!this.hasEditPermission(auth, contentPath)) {
			throw new UnauthorizedException("User is not allowed to edit this content", auth);
		}
		
	}
	
	/**
	 * Check whether the user is allowed to approve contentPath edited by user
	 * with editUserId (or null if anonymous).
	 * @param auth current users' auth token
	 * @param editUserId the used who edited the content which needs to be approved
	 * @param contentPath the content path of the content to be approved
	 * @return true if the given user is allowed to approve; false otherwise.
	 */
	private boolean hasApprovalPermission(AuthenticationToken auth,
									   	  @Nullable Integer editUserId,
									   	  @NotNull String contentPath) {

		this.assertContentEnabled(contentPath);
		
		List<Role> editorRoles = this.authService.getUserRoles(editUserId);
		
		return this.getContentAccess(contentPath).stream().allMatch(a -> {
			if (SecurityUtil.isAdmin(auth)) {
				return true;
			}
			if (SecurityUtil.isAnonymous(auth)) {
				return a.getAnonApprovalByRole() == null;
			}
			for (UserContentAccessRole ra : a.getRoleAccess()) {
				if (!editorRoles.stream().anyMatch(er -> er.equals(ra.getRole()))) {
					continue;
				}
				if (SecurityUtil.hasRole(auth, ra.getApprovalByRole())) {
					return true;
				}
			}
			return false;
		});
	
	}
	
	/**
	 * Check whether the user is allowed to approve contentPath edited by user with editUserId (or null if anonymous).
	 * Guarantees to throw an {@link UnauthorizedException} if this is not the case.
	 * @param auth current users' auth token
	 * @param editUserId the used who edited the content which needs to be approved
	 * @param contentPath the content path of the content to be approved
	 * @throws UnauthorizedException if content may not be approved by this user
	 */
	private void assertApprovalPermission(@NotNull AuthenticationToken auth,
										  @Nullable Integer editUserId,
										  @NotNull String contentPath) throws UnauthorizedException {
		
		if (!this.hasApprovalPermission(auth, editUserId, contentPath)) {
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
		
		this.assertApprovalPermission(authToken, editUserId, metadata.getContentPath());
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
		this.assertViewPermission(authToken, contentPath);
		
		List<UserContent> content = this.contentMapper.findContent(
			contentPath,
			UserContentLanguage.fromId(contentLangId),
			authToken.getId(),
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
		
		this.assertEditPermission(authToken, contentPath);
		this.throttleContentSaving(authToken);
		this.contentValidator.check(contentPath, cmsContent);
		cmsContent.sanitize();
		
		UserIdentity user = UserIdentity.fromAuthToken(authToken);
		UserContent content;
		try {
			content = CmsContent.toUserContent(
					cmsContent,
					contentPath,
					contentLanguage,
					isDraft ? UserContentStatus.DRAFT : UserContentStatus.AWAITING_APPROVAL,
					user,
					this.jsonObjectMapper.writerWithView(CmsContentViews.Persist.class)
					);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Can't serialize CmsContent back to string", e);
		}
		
		boolean doUpdate = false;
		
		// instant-approval
		if (content.getStatus() == UserContentStatus.AWAITING_APPROVAL &&
			this.hasApprovalPermission(authToken, user.getId(), contentPath)) { 
			content.setStatus(UserContentStatus.APPROVED);
			content.setApproveDate(new Date());
			content.setApproveUser(user);
		}
		
		// check if draft exists
		UserContentMetadata userDraft = null;
		if (content.getHistoryId() != null) {
			userDraft = this.contentMapper.findContentUserDraft(
					content.getContentPath(),
					content.getHistoryId(),
					content.getContentLanguage(),
					user.getId()
					);
			if (userDraft != null) {
				content.setId(userDraft.getId());
				content.setHistoryId(userDraft.getHistoryId());
				doUpdate = true;
			}
		}
		
		// validation/update of prev/next id
		if (content.getHistoryId() == null) { // insert new
			this.assertValidPrevNextIds(content, authToken);
		} else { // update existing
			this.assertNewestContentBase(content, authToken);
			this.updatePrevNextIds(content, authToken);
		}
		
		// overwrite existing entry if keepHistory = false
		UserContentMetadata baseContent = this.contentMapper.findContentMetadataById(content.getHistoryId());
		if (content.getStatus() == UserContentStatus.APPROVED
				&& content.getHistoryId() != null) {
			boolean keepHistory = this.getContentAccess(contentPath)
					.stream()
					.anyMatch(ca -> ca.isKeepHistory());
			if (!keepHistory) {
				content.setId(baseContent.getId());
				content.setHistoryId(baseContent.getHistoryId());
				if (userDraft != null) {
					this.contentMapper.deleteContentUserDraft(userDraft.getId());					
				}
				doUpdate = true;
			}
		}
		
		try {
			if (doUpdate) {
				if (this.contentMapper.updateContent(content) == 0) {
					throw new InternalException("No content was inserted/updated");
				}
			} else {
				this.contentMapper.insertContent(content);
			}
		} catch (DataIntegrityViolationException e) {
			throw new BadRequestException("Invalid content metadata");
		}
		
		if (content.getStatus() == UserContentStatus.APPROVED) {
			this.contentMapper.stitchContent(content, content.getId(), content.getId());
			this.publishContentUpdateFiles(content, cmsContent, authToken);
		}
		
		return content;
		
	}
	
	/**
	 * Set the state of the content with given UID to AWAITING_DELETION, or,
	 * if the current user has approval permission, DELETED.
	 * If the previous and next content entries are still pointing to that
	 * entry, update them to directly reference each other.
	 * @param authToken current users' authentication
	 * @param contentUid content UID to delete
	 * @return the deleted user content
	 */
	@Transactional
	public void deleteContent(@NotNull AuthenticationToken authToken,
							  long contentUid) {

		UserContentMetadata contentMetadata = this.contentMapper.findContentMetadataById(contentUid);
		if (contentMetadata == null) {
			throw new BadRequestException("No content with given UID found.");
		}
		
		this.assertEditPermission(authToken, contentMetadata.getContentPath());
		
		UserContentStatus updateStatus = UserContentStatus.UNAPPROVED_AWAITING_DELETION;
		if (contentMetadata.getStatus() == UserContentStatus.APPROVED) {
			updateStatus = UserContentStatus.APPROVED_AWAITING_DELETION;
		}
		if (this.hasApprovalPermission(authToken, authToken.getId(), contentMetadata.getContentPath())) {
			updateStatus = UserContentStatus.DELETED;
		}
		
		this.contentMapper.updateContentStatus(contentUid, updateStatus);
		this.contentMapper.stitchContent(contentMetadata, contentMetadata.getNextId(), contentMetadata.getPreviousId());

		List<Long> excludeFileUids = new LinkedList<>();
		UserContent newContent = this.contentMapper
				.findContentForHistoryId(contentMetadata.getHistoryId());
		if (newContent != null) {
			try {
				CmsContent newCmsContent = this.jsonObjectMapper.readValue(newContent.getContent(), CmsContent.class);
				excludeFileUids.addAll(this.getFileUidsForCmsContent(newCmsContent));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(
						"Invalid content (id: " + newContent.getId() +
						"); can't delete earlier content entry (id: " +
						newContent.getHistoryId() + ")"
				);
			}
		}
		this.deactivateFiles(contentUid, excludeFileUids);
		
	}
	
	/**
	 * Get the given content file by id if the given current user is allowed to view it.
	 * There are three cases in which a user is allowed to view a file:
	 * - the user is the same user who uploaded the file
	 * - the user has viewing permission, the file is active and the associated content is approved
	 * - the user has viewing permission, the file is inactive and the user has approval permission
	 * @param authToken current user auth token (pass null if user is anonymous)
	 * @param contentPath content path of the associated content
	 * @param fileId id of the specific file to get
	 * @return {@link UserContentFile}
	 * @throws BadRequestException see {@link #assertViewPermission(AuthenticationToken, String)}
	 * @throws UnauthorizedException if given user may not view the file
	 */
	public @NonNull UserContentFile getContentFile(@Nullable AuthenticationToken authToken,
							   					   @NonNull String contentPath,
							   					   long fileId) throws BadRequestException, UnauthorizedException {
		
		this.assertViewPermission(authToken, contentPath);
		
		UserContentFile file = this.contentMapper.findContentFile(fileId, contentPath);
		UserContentMetadata metadata = this.contentMapper.findContentMetadataById(file.getActivateContentUid());
		UserIdentity editUser = metadata.getUser();
		
		// same user
		if (authToken != null && file.getUserId() == authToken.getId()) {
			return file;
			
		// active file and approved content
		} else if (file.isActive() && file.getActivateContentUid() != null && metadata.getStatus() == UserContentStatus.APPROVED) {
			return file;
			
		// current user is approver
		} else if (this.hasApprovalPermission(authToken, editUser == null ? null : editUser.getId(), contentPath)) {
			return file;
		}
		
		throw new UnauthorizedException("Unauthorized access to file", authToken);
		
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
	 * Check if the given user content references valid history entry,
	 * predecessor and successor and throw {@link BadRequestException} if
	 * they are invalid.
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
				userContent.getStatus() == UserContentStatus.DRAFT ? true : false
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
	 */
	public void assertValid(CmsContent content, Errors errors) throws ValidationException {
		
		errors.pushNestedPath("content");
		content.validate(errors);
		errors.popNestedPath();
		
		if (errors.hasErrors()) {
			throw new ValidationException("CMS content validation failed.", errors);
		}
		
	}
	
	/**
	 * Get a single file for the given content path.
	 * @param contentPath content path this file is associated with
	 * @param fileUid id of file to find
	 * @return {@link ResponseFile} holding the requested file
	 * @throws NotFoundException if no file is found
	 * @throws UnauthorizedException if the file is inactive
	 * @throws InternalException if the file can't be found on the FS, can't
	 * 							 be read, or the file path is outside of the
	 * 							 {@link UserContentConfiguration#FILE_ROOT}.
	 */
	public ResponseFile getFile(@Nullable AuthenticationToken authToken,
								@NotNull String contentPath,
								long fileUid) {
		
		this.assertViewPermission(authToken, contentPath);
		
		UserContentFile file = this.contentMapper.findContentFile(fileUid, contentPath);
		if (file == null) {
			throw new NotFoundException("No file found.");
		} else if (!file.isActive() && (authToken == null || (file.getUserId() != authToken.getId() && !SecurityUtil.isAdmin(authToken)))) {
			throw new UnauthorizedException("File " + fileUid + " is not active and may therefore not be viewed.", authToken);
		}
		
		Path vPath = UserContentConfiguration.FILE_ROOT
				.resolve(file.getPath())
				.toAbsolutePath()
				.normalize();
		
		try {
			if (!vPath.toFile().exists()) {
				throw new InternalException("File could not be found: " + vPath);
			}			
		} catch (SecurityException e) {
			throw new InternalException("Not allowed to read file: " + vPath);
		}

		if (vPath.startsWith(UserContentConfiguration.FILE_ROOT.toAbsolutePath())) {
				return new ResponseFile(vPath, file.getType());				
		}
		
		throw new InternalException("Path of stored content file is outside of : " + file.getPath());
		
	}
	
	/**
	 * Save a new file to the service's filesystem and store a reference to it
	 * in the database.
	 * Files created using this method are initially inactive and are
	 * automatically activated once a file component with the returned file UID
	 * is published.
	 * @param authToken autentication token of current user
	 * @param contentPath content path this file belongs to
	 * @param file contents of the file (in multipart-form format)
	 * @return the new files' id
	 */
	public long saveNewFile(@Nullable AuthenticationToken authToken,
							@NotNull String contentPath,
							@NotNull MultipartFile file) {
		
		this.assertEditPermission(authToken, contentPath);
		
		final Path absoluteRoot = UserContentConfiguration.FILE_ROOT.toAbsolutePath();
		Path targetPath = absoluteRoot.resolve(UUID.randomUUID().toString());
		
		try {
			
			file.transferTo(targetPath);
			
			final UserContentFile insertFile = new UserContentFile(
					absoluteRoot.relativize(targetPath).toString(),
					file.getContentType(),
					authToken == null ? null : authToken.getId()
			);
			
			this.contentMapper.insertContentFile(
					contentPath,
					insertFile
			);
			
			return insertFile.getId();

		} catch (Throwable e) {
			try {
				targetPath.toFile().delete();				
			} catch (Throwable t) {}
			
			throw new RuntimeException("Failed to save content file.", e);
		}
		
	}
	
	/**
	 * Set the files within the given user content to active, so they
	 * can be viewed not only the the uploader.
	 * Important:	Make sure the user of given authToken has permission
	 * 				to do so.
	 * TODO dont deactivate all, then activate all; use diff
	 * @param content newly updated user content holding content and metadata
	 * @param cmsContent new CMS content to be checked for files
	 * @param authToken authentication of user with permission to publish
	 * 					files for the given user content
	 * @throws BadRequestException if the given CMS content contains any
	 * 							   invalid file references.
	 */
	private void publishContentUpdateFiles(UserContent content,
							 CmsContent cmsContent,
							 AuthenticationToken authToken) throws BadRequestException{
		
		List<Long> files = this.getFileUidsForCmsContent(cmsContent);
		if (content.getHistoryId() != null) {
			this.deactivateFiles(content.getId(), files);
		}
		
		files.stream().forEach(fileUid -> {
			int updatedRows = this.contentMapper.updateActivateContentFile(
					fileUid,
					content.getId(),
					authToken == null ? null : authToken.getId()
			);
			if (updatedRows == 0) {
				throw new BadRequestException(
						"Content contains invalid file reference.",
						ClientError.CONTENT_FILE_INVALID_FILE
						);
			}
		});
		
	}
	
	/**
	 * Deactivate all files not present in the given content entry.
	 * @param contentUid content UID to check for files
	 * @param excludeFiles file IDs to not deactivate
	 */
	private void deactivateFiles(long contentUid, List<Long> excludeFiles) {
		
		List<UserContentFile> previousFiles = this.contentMapper.findActiveContentFilesForContentUid(contentUid);
		previousFiles.stream()
			.filter(f -> !excludeFiles.contains(f.getId()))
			.forEach(f -> {
				this.contentMapper.updateDeactivateContentFile(
						f.getId(),
						contentUid
				);
			});
		
	}
	
	/**
	 * Get all file IDs of {@link FileComponent}s in given cms content.
	 * @param cmsContent content to search
	 * @return list of file IDs contained in given content
	 */
	private List<Long> getFileUidsForCmsContent(@NotNull CmsContent cmsContent) {
		return cmsContent
				.getContent()
				.gatherAllElements()
				.stream()
				.filter(e -> e instanceof FileComponent)
				.map(e -> ((FileComponent) e).getFileUid())
				.collect(Collectors.toUnmodifiableList());
	}

}
