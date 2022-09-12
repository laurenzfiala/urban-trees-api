package at.sparklingscience.urbantrees.service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.PathMatcher;

import at.sparklingscience.urbantrees.cms.UserContentConfiguration;
import at.sparklingscience.urbantrees.cms.validation.UserContentValidator;
import at.sparklingscience.urbantrees.domain.User;
import at.sparklingscience.urbantrees.domain.UserContentAccess;
import at.sparklingscience.urbantrees.domain.UserContentAccessRole;
import at.sparklingscience.urbantrees.domain.UserContentSaveAmount;
import at.sparklingscience.urbantrees.domain.UserIdentity;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.TooManyRequestsException;
import at.sparklingscience.urbantrees.exception.UnauthorizedException;
import at.sparklingscience.urbantrees.mapper.UserContentMapper;
import at.sparklingscience.urbantrees.security.SecurityUtil;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;

/**
 * Service for deciding if view/edit/applroval permissions are met
 * for user content.
 * 
 * @author Laurenz Fiala
 * @since 2022/02/20
 */
@Service
public class UserContentAccessService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserContentAccessService.class);
	
	@Autowired
	private PathMatcher userContentPathMatcher;
	
	@Autowired
    private UserContentValidator contentValidator;
	
	@Autowired
    private AuthenticationService authService;
	
	@Autowired
    private UserContentMapper contentMapper;
	
	@Value("${at.sparklingscience.urbantrees.userContent.maxSavesPerUserPerDay}")
	private int maxSavesPerUserPerDay;
	
	/**
	 * TODO
	 * @param access
	 * @return
	 */
	public boolean shouldKeepHistory(String contentPath) {
		return this.shouldKeepHistory(this.getContentAccess(contentPath));
	}
	
	/**
	 * TODO
	 * @param access
	 * @return
	 */
	public boolean shouldKeepHistory(List<UserContentAccess> access) {
		return access.stream().anyMatch(a -> a.isKeepHistory());
	}
	
	/**
	 * Get all applicable content access entries for the given content path.
	 * @param contentPath content path
	 * @return list of content access configs
	 */
	public List<UserContentAccess> getContentAccess(String contentPath) throws BadRequestException {
		
		this.contentValidator.checkPath(contentPath);
		
		String[] pathSegments = contentPath.split(UserContentConfiguration.CONTENT_PATH_SEPARATOR);
		String pathExp = Stream.of(pathSegments)
							   .filter(s -> !s.isEmpty())
							   .map(s -> "(\\/(" + s + "|\\{.+?\\}|\\*{1,2}))?")
							   .collect(Collectors.joining());
		pathExp = "^" + pathExp + "$";
		
		List<UserContentAccess> accessCandidates = this.contentMapper.findContentAccessByExp(pathExp);
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
	public boolean isContentEnabled(String contentPath) {
		return this.getContentAccess(contentPath)
					.stream()
					.allMatch(ca -> ca.isEnabled());
	}
	
	/**
	 * Check that the given content is enabled.
	 * @param contentPath content path
	 * @throws BadRequestException if the content is disabled.
	 */
	public void assertContentEnabled(String contentPath) throws BadRequestException {
		
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
	public boolean hasViewPermission(AuthenticationToken auth, String contentPath) throws UnauthorizedException {
		this.assertContentEnabled(contentPath);
		return this.getContentAccess(contentPath)
					.stream()
					.allMatch(ca -> this.hasViewPermission(auth, ca));
	}
	
	/**
	 * TODO
	 * @param auth
	 * @param access
	 * @return
	 * @throws UnauthorizedException
	 */
	public boolean hasViewPermission(@Nullable AuthenticationToken auth,
									 @NonNull UserContentAccess access) throws UnauthorizedException {

		if (SecurityUtil.isAdmin(auth)) {
			return true;
		}
		if (SecurityUtil.isAnonymous(auth)) {
			return access.isAnonAllowView();
		}
		Boolean roleAllow = null;
		for (UserContentAccessRole ra : access.getRoleAccess()) {
			if (SecurityUtil.hasRole(auth, ra.getRole())) {
				if (ra.isAllowView() && roleAllow == null) {
					roleAllow = true;
				} else if (!ra.isAllowView()) {
					return false;
				}
			}
		}
		return roleAllow == null ? access.isUserAllowView() : roleAllow;
			
		
	}
	
	/**
	 * Check whether the given user is allowed to view given user content.
	 * Guarantees to throw an {@link UnauthorizedException} if this is not the case.
	 * @param auth current users' auth token.
	 * @param contentPath content to check permissions for.
	 * @throws UnauthorizedException if content may not be viewed by the given user.
	 */
	public void assertViewPermission(AuthenticationToken auth, String contentPath) throws UnauthorizedException {
		
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
	public boolean hasEditPermission(AuthenticationToken auth, String contentPath) throws UnauthorizedException {

		this.assertContentEnabled(contentPath);
		return this.getContentAccess(contentPath)
					.stream()
					.allMatch(ca -> this.hasEditPermission(auth, ca));
		
	}
	
	/**
	 * TODO
	 * @param auth
	 * @param access
	 * @return
	 * @throws UnauthorizedException
	 */
	public boolean hasEditPermission(@Nullable AuthenticationToken auth, 
									 @NonNull UserContentAccess access) throws UnauthorizedException {

		if (SecurityUtil.isAdmin(auth)) {
			return true;
		}
		if (SecurityUtil.isAnonymous(auth)) {
			return access.isAnonAllowEdit();
		}
		Boolean roleAllow = null;
		for (UserContentAccessRole ra : access.getRoleAccess()) {
			if (SecurityUtil.hasRole(auth, ra.getRole())) {
				if (ra.isAllowEdit() && roleAllow == null) {
					roleAllow = true;
				} else if (!ra.isAllowEdit()) {
					return false;
				}
			}
		}
		return roleAllow == null ? access.isUserAllowEdit() : roleAllow;
		
	}
	
	/**
	 * Check whether the given user is allowed to edit given user content.
	 * Guarantees to throw an {@link UnauthorizedException} if this is not the case.
	 * @param auth current users' auth token.
	 * @param contentPath content to check permissions for.
	 * @throws UnauthorizedException if content may not be edited by the given user.
	 */
	public void assertEditPermission(AuthenticationToken auth, String contentPath) throws UnauthorizedException {
		
		if (!this.hasEditPermission(auth, contentPath)) {
			throw new UnauthorizedException("User is not allowed to edit this content", auth);
		}
		
	}
	
	/**
	 * TODO
	 * Check whether the user is allowed to approve contentPath edited by user
	 * with editUserId (or null if anonymous).
	 * @param auth current users' auth token
	 * @param editUserId the used who edited the content which needs to be approved
	 * @param contentPath the content path of the content to be approved
	 * @return true if the given user is allowed to approve; false otherwise.
	 */
	public boolean hasApprovalPermission(@Nullable AuthenticationToken auth,
									   	 @Nullable UserIdentity editorUserIdentity,
									   	 @NonNull String contentPath) {

		User editor = this.authService.findUser(editorUserIdentity);
		return this.hasApprovalPermission(auth, editor, contentPath);
	
	}

	/**
	 * TODO
	 * @param auth
	 * @param editor
	 * @param contentPath
	 * @return
	 */
	public boolean hasApprovalPermission(@Nullable AuthenticationToken auth,
									   	 @Nullable User editor,
									   	 @NonNull String contentPath) {

		this.assertContentEnabled(contentPath);
		
		return this.getContentAccess(contentPath)
					.stream()
					.allMatch(ca -> this.hasApprovalPermission(auth, editor, ca));
	
	}
	
	/**
	 * TODO
	 * @param auth
	 * @param editorUserIdentity
	 * @param access
	 * @return
	 */
	public boolean hasApprovalPermission(@Nullable AuthenticationToken auth,
		   	 @Nullable UserIdentity editorUserIdentity,
		   	 @NonNull UserContentAccess access) {

		User editor = this.authService.findUser(editorUserIdentity.getId());
		return this.hasApprovalPermission(auth, editor, access);
	
	}
	
	/**
	 * TODO
	 * @param auth
	 * @param editor
	 * @param access
	 * @return
	 */
	public boolean hasApprovalPermission(@Nullable AuthenticationToken auth,
										 @Nullable User editor,
										 @NonNull UserContentAccess access) {

		if (SecurityUtil.isAdmin(auth)) {
			return true;
		}
		if (editor == null) {
			return access.getAnonApprovalByRole() == null || SecurityUtil.hasRole(auth, access.getAnonApprovalByRole());
		}
		for (UserContentAccessRole ra : access.getRoleAccess()) {
			if (!editor.getRoles().stream().anyMatch(er -> er.equals(ra.getRole()))) {
				continue;
			}
			if (ra.getApprovalByRole() == null) {
				return true;
			} else if (SecurityUtil.hasRole(auth, ra.getApprovalByRole())) {
				return true;
			}
		}
		return access.getUserApprovalByRole() == null || SecurityUtil.hasRole(auth, access.getUserApprovalByRole());
	
	}
	
	/**
	 * Check whether the user is allowed to approve contentPath edited by user with editUserId (or null if anonymous).
	 * Guarantees to throw an {@link UnauthorizedException} if this is not the case.
	 * @param auth current users' auth token
	 * @param editor the user> who edited the content which needs to be approved
	 * @param contentPath the content path of the content to be approved
	 * @throws UnauthorizedException if content may not be approved by this user
	 */
	public void assertApprovalPermission(@NotNull AuthenticationToken auth,
										 @Nullable UserIdentity editor,
										 @NotNull String contentPath) throws UnauthorizedException {
		
		UserContentAccess access = this.contentMapper.findContentAccessForPath(contentPath);
		boolean canAccess = true;
		if (access == null) {
			canAccess = this.hasApprovalPermission(auth, editor, contentPath);
		} else {
			canAccess = this.hasApprovalPermission(auth, editor, access);
		}
		if (!canAccess) {
			throw new UnauthorizedException("User is not allowed to view this content", auth);
		}
		
	}

	
	/**
	 * Check whether the user is allowed to save content.
	 * If the user exceeded their daily quota (see config: at.sparklingscience.urbantrees.userContent.maxSavesPerUserPerDay)
	 * a {@link TooManyRequestsException} is thrown.
	 * @param authToken users' auth token
	 * @throws TooManyRequestsException if the user may not insert a new content to the DB (exceeded their quota)
	 */
	public void throttleContentSaving(AuthenticationToken authToken) throws TooManyRequestsException {
		
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

}
