package at.sparklingscience.urbantrees.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.sun.jdi.InternalException;

import at.sparklingscience.urbantrees.cms.action.UserContentActions;
import at.sparklingscience.urbantrees.domain.User;
import at.sparklingscience.urbantrees.domain.UserContent;
import at.sparklingscience.urbantrees.domain.UserContentAccess;
import at.sparklingscience.urbantrees.domain.UserContentManagerAccess;
import at.sparklingscience.urbantrees.domain.UserContentMetadata;
import at.sparklingscience.urbantrees.domain.UserContentStatus;
import at.sparklingscience.urbantrees.domain.UserIdentity;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.UnauthorizedException;
import at.sparklingscience.urbantrees.mapper.UserContentMapper;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;

/**
 * Service holds methods related to user content manager like
 * approval, listing unapproved entries, etc.
 * 
 * @author Laurenz Fiala
 * @since 2022/02/21
 */
@Service
public class UserContentManagerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserContentManagerService.class);
	
	@Autowired
	private UserContentService contentService;
	
	@Autowired
	private UserContentAccessService accessService;
	
	@Autowired
    private UserContentFileService fileService;
	
	@Autowired
    private AuthenticationService authService;
	
	@Autowired
	private UserContentActions contentActions;
	
	@Autowired
    private UserContentMapper contentMapper;
	
	/**
	 * Get all published content that is viewable to the current user.
	 * If pathExp is given, the contents matching that expression must be
	 * viewable, otherwise an {@link UnauthorizedException} is thrown.
	 * @param pathExp (optional) content access path expression
	 * @param authToken current user
	 * @return list of viewable content access entries
	 * @throws UnauthorizedException if pathExp is given and associated access
	 * 								 entries are not viewable
	 */
	public List<UserContentManagerAccess> getContentAccessViewable(@Nullable String pathExp,
																   @NonNull AuthenticationToken authToken) {
		
		List<UserContentAccess> accessList = this.contentMapper.findContentAccess();
		if (pathExp != null) {
			accessList = accessList
					.stream()
					.filter(ca -> ca.getContentPath().equals(pathExp))
					.collect(Collectors.toList());
			if (!accessList
					.stream()
					.allMatch(ca -> this.accessService.hasViewPermission(authToken, ca))) {
				throw new UnauthorizedException("You are not allowed to view the requested content", authToken);
			}
		}
		
		return accessList
			.stream()
			.filter(ca -> this.accessService.hasViewPermission(authToken, ca))
			.map(ca -> {
				int approved = this.contentMapper.countContentApprovedForAccessId(ca.getId());
				return new UserContentManagerAccess(ca, approved);
			})
			.collect(Collectors.toUnmodifiableList());
		
	}
	
	/**
	 * TODO
	 * @param authToken
	 * @return
	 */
	public List<UserContentManagerAccess> getContentAccessApprovable(@Nullable String pathExp,
																	 @NonNull AuthenticationToken authToken) {
		
		User user = this.authService.findUser(authToken.getId());
		List<UserContentAccess> accessList = this.contentMapper.findContentAccessForApprover(user);
		if (pathExp != null) {
			List<UserContentAccess> accessListByExp = this.contentMapper.findContentAccessByExp(pathExp);
			boolean allApprovable = accessListByExp.stream()
			   .map(ca -> ca.getId())
			   .allMatch(
					   accessList.stream()
					       .map(ca -> ca.getId())
					       .collect(Collectors.toSet())
					       ::contains
			       );
			if (!allApprovable) {
				throw new UnauthorizedException("You are not allowed to view the requested content", authToken);
			}
		}		
		
		return this.contentMapper.findContentAccessForApprover(user)
				.stream()
				.map(ca -> {
					int approvable = this.contentMapper.countContentApprovableForAccessId(ca.getId());
					return new UserContentManagerAccess(ca, approvable);
				}).collect(Collectors.toUnmodifiableList());
		
	}
	
	/**
	 * TODO
	 * @param accessId
	 * @param status
	 * @param authToken
	 * @return
	 */
	public List<UserContentMetadata> getContentViewable(long accessId,
												  		@Nullable UserContentStatus status,
												  		@NonNull AuthenticationToken authToken) {

		List<UserContentStatus> statuses = List.of(UserContentStatus.APPROVED);
		if (status != null) {
			statuses = List.of(status);
		}
		
		return this.contentMapper.findContentMetadataForAccessId(accessId, statuses)
				.stream()
				.filter(c -> {
					return this.accessService.hasViewPermission(authToken, this.contentMapper.findContentAccessById(accessId));
				})
				.collect(Collectors.toUnmodifiableList());
		
	}
	
	/**
	 * TODO
	 * @param accessId
	 * @param status
	 * @param authToken
	 * @return
	 */
	public List<UserContentMetadata> getContentApprovable(long accessId,
												  		  @Nullable UserContentStatus status,
												  		  @NonNull AuthenticationToken authToken) {

		List<UserContentStatus> statuses = UserContentStatus.transienT();
		if (status != null) {
			statuses = List.of(status);
		}

		return this.contentMapper.findContentMetadataForAccessId(accessId, statuses)
				.stream()
				.filter(c -> {
					User editor = this.authService.findUser(c.getUser().getId());
					return this.accessService.hasApprovalPermission(authToken, editor, this.contentMapper.findContentAccessById(accessId));
				})
				.collect(Collectors.toUnmodifiableList());
		
	}
	
	/**
	 * TODO
	 * @param accessId
	 * @param status
	 * @param authToken
	 * @return
	 */
	public UserContent getContent(long contentUid,
								  @NonNull AuthenticationToken authToken) {

		UserContent content = this.contentMapper.findContentById(contentUid);
		this.accessService.assertViewPermission(authToken, content.getContentPath());
		
		return content;
		
	}
	
	/**
	 * TODO
	 * @param contentUid
	 * @param approve
	 * @param authToken
	 * @return
	 */
	public UserContentStatus updateContentStatus(long contentUid,
									boolean approve,
									@NonNull AuthenticationToken authToken) {
		
		UserIdentity user = UserIdentity.fromAuthToken(authToken);
		UserContent userContent = this.contentMapper.findContentById(contentUid);
		if (userContent == null) {
			throw new BadRequestException("User content not found.");
		}
		if (userContent.getStatus().isPermanent()) {
			throw new BadRequestException("Content has invalid status to " + (approve ? "approve" : "deny") + ".");
		}
		
		this.accessService.assertApprovalPermission(authToken, user, userContent.getContentPath());
		UserContentStatus oldStatus = userContent.getStatus();
		UserContentStatus newStatus = approve ? oldStatus.next() : oldStatus.previous();
		int updatedRows = this.contentMapper.updateContentStatus(contentUid, user.getId(), newStatus);
		if (updatedRows == 0) {
			throw new InternalException("No applicable content found and therefore not updated.");
		}
		if (newStatus == UserContentStatus.DELETED) {
			this.contentService.deleteContent(user, userContent);
		}
		
		// post-save actions
		this.fileService.cleanUpFiles(userContent);
		this.contentActions.doActions(userContent.getContentPath(), userContent, authToken);
		
		return newStatus;
		
	}

}
