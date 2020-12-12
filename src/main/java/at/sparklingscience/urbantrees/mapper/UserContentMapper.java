package at.sparklingscience.urbantrees.mapper;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.GrantedAuthority;

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
	
}
