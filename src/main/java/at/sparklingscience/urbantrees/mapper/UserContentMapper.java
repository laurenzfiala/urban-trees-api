package at.sparklingscience.urbantrees.mapper;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.GrantedAuthority;

import at.sparklingscience.urbantrees.domain.Role;
import at.sparklingscience.urbantrees.domain.UserContent;
import at.sparklingscience.urbantrees.domain.UserContentMetadata;

/**
 * Mybatis mapping interface.
 * For user-content-related operations.
 * 
 * @author Laurenz Fiala
 * @since 2018/03/13
 */
@Mapper
public interface UserContentMapper {

	/**
	 * Check whether the given content is enabled.
	 * @param contentId content id
	 * @return true if enabled; false otherwise.
	 */
	boolean isContentEnabled(@Param("contentId") String contentId);
	
	/**
	 * Find single user content metadata by its internal id.
	 * @param contentUid internal id
	 * @return metadata of one content entry
	 */
	UserContentMetadata findContentMetadataById(@Param("contentUid") long contentUid);
	
	/**
	 * Find all current approved content entries for the given content id in order of content_order.
	 * @param id content id to search for
	 * @return list of matched user contents
	 */
	List<UserContent> findAllContent(@Param("contentId") String contentId);
	
	
	/**
	 * Find current approved content and all previous approved contents for the given contentId and contentOrder.
	 * @param contentId content id to look for
	 * @param contentOrder order of the content to look for
	 * @return list of content history starting with the newest approved entry
	 */
	List<UserContent> findContentHistory(
			@Param("contentId") String contentId,
			@Param("contentOrder") int contentOrder
			);
	
	
	/**
	 * Find all past user content edits (one per content id).
	 * @param userId user for whom to find the history for
	 * @param contentIdPrefix only find content ids which start with given string
	 * @param limit max amount of history entries
	 * @return list of cms content history for given user
	 */
	List<UserContentMetadata> findContentUserHistory(
			@Param("userId") int userId,
			@Param("contentIdPrefix") String contentIdPrefix,
			@Param("limit") int limit
			);

	/**
	 * Check whether the given content id may be viewed by any of the given roles.
	 * 
	 * Note: if the roles list is null, only anonymous users' permissios are checked.
	 * 		 if the roles list is empty, default user permissions are checked also.
	 * 
	 * Important: if you check for anonymous access, roles must be null.
	 * 			  if you check for user access, roles must not be null.
	 * @param contentId content id to check permissions for
	 * @param grantedAuthorities list of roles (if any role has a permission, it is granted); pass null to check for anonymous access
	 * @return true if any of the given roles allow to view the content or anonymous access is allowed; false otherwise
	 */
	boolean canViewContent(
			@Param("contentId") String contentId,
			@Param("grantedAuthorities") Collection<? extends GrantedAuthority> grantedAuthorities
			);

	/**
	 * Check whether the given content id may be edited by any of the given roles.
	 * 
	 * Note: if the roles list is null, only anonymous users' permissios are checked.
	 * 		 if the roles list is empty, default user permissions are checked also.
	 * 
	 * Important: if you check for anonymous access, roles must be null.
	 * 			  if you check for user access, roles must not be null.
	 * @param contentId content id to check permissions for
	 * @param grantedAuthorities list of roles (if any role has a permission, it is granted); pass null to check for anonymous access
	 * @return true if any of the given roles allow to edit the content or anonymous access is allowed; false otherwise
	 */
	boolean canEditContent(
			@Param("contentId") String contentId,
			@Param("grantedAuthorities") Collection<? extends GrantedAuthority> grantedAuthorities
			);

	/**
	 * Check whether the given content id (edited by user/anon with editorGrantedAuthorities)
	 * may be approved by any of grantedAuthorities.
	 * 
	 * Note: if the editor roles list is null, the user is assumed to be anonymous.
	 * 		 if the editor roles list is empty, default approval permissions are checked also.
	 * 
	 * Important: if you check for approval of anonymous editor, editorGrantedAuthorities must be null.
	 * 			  if you check for approval of non-anonymous editor, editorGrantedAuthorities must not be null.
	 * 			  grantedAuthorities must not be null.
	 * @param contentId content id to check permissions for
	 * @param editorGrantedAuthorities list of roles of the user who edited the content. must be null if editor was anonymous
	 * @param grantedAuthorities list of roles of the approving user (may be the same as editorGrantedAuthorities)
	 * @return true if the editor/approver roles allow content approval; false otherwise
	 */
	boolean canApproveContent(
			@Param("contentId") String contentId,
			@Param("editorRoles") List<Role> editorRoles,
			@Param("grantedAuthorities") Collection<? extends GrantedAuthority> grantedAuthorities
			);
	
	/**
	 * Approve the given content by given user.
	 * @param contentUid internal id of content (not content id)
	 * @param userId user id of approval user
	 * @return nr. of rows updated. Note: must be 1. If it is 0 and content exists,
	 * 		   the content may have already been approved.
	 */
	int approveContentById(@Param("contentUid") long contentUid,
						   @Param("userId") int userId);
	
}
