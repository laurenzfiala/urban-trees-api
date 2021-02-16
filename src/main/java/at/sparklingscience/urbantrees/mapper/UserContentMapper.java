package at.sparklingscience.urbantrees.mapper;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.GrantedAuthority;

import at.sparklingscience.urbantrees.domain.Role;
import at.sparklingscience.urbantrees.domain.UserContent;
import at.sparklingscience.urbantrees.domain.UserContentFile;
import at.sparklingscience.urbantrees.domain.UserContentLanguage;
import at.sparklingscience.urbantrees.domain.UserContentMetadata;
import at.sparklingscience.urbantrees.domain.UserContentSaveAmount;

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
	 * Find meta info on content entries in the DB saved by the given user in the last 24 hours.
	 * The query does not differentiate between drafts and published content.
	 * @param userId user id to check or null to check for anonymous saves
	 * @return see {@link UserContentSaveAmount}
	 */
	UserContentSaveAmount findSavedContentAmountForUserId(@Param("userId") Integer userId);
	
	/**
	 * Find single user content metadata by its internal id.
	 * @param contentUid internal id
	 * @return metadata of one content entry
	 */
	UserContentMetadata findContentMetadataById(@Param("contentUid") long contentUid);
	
	/**
	 * Find the metadata for the newest content entry for content id/order/lang.
	 * @param contentId content id
	 * @param contentOrder content order
	 * @param contentLang content language
	 * @return metadata of the newest content entry
	 */
	UserContentMetadata findContentMetadata(@Param("contentId") String contentId,
											@Param("contentOrder") int contentOrder,
											@Param("contentLang") UserContentLanguage contentLang);
	
	/**
	 * Find all current approved content entries for the given content id in order of
	 * content_order and with given language.
	 * @param id content id to search for
	 * @param id content language to search for
	 * @return list of matched user contents
	 */
	List<UserContent> findAllContent(@Param("contentId") String contentId,
									 @Param("contentLang") UserContentLanguage contentLang);
	
	
	/**
	 * Find current approved content and all previous approved contents for the given contentId and contentOrder.
	 * @param contentId content id to look for
	 * @param contentOrder order of the content to look for
	 * @return list of content history starting with the newest approved entry
	 */
	List<UserContent> findContentHistory(
			@Param("contentId") String contentId,
			@Param("contentOrder") int contentOrder,
			@Param("contentLang") UserContentLanguage contentLang
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
	 * @param editorRoles list of roles of the user who edited the content. must be null if editor was anonymous
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
	
	/**
	 * Insert a new content entry into the DB and update the given contents id.
	 * @param content content to insert. id field is updated on insert.
	 * @return number of rows inserted (always 1).
	 */
	int insertContent(@Param("c") UserContent content);
	
	/**
	 * Update an exitsing content draft in the DB.
	 * @param content content to set
	 * @return number of rows updated (<= 1).
	 */
	int updateContentDraft(@Param("c") UserContent content);
	
	/**
	 * Find a previously stored draft for the given user/content/lang/order combination.
	 * Since only one draft per content id per user can be stored, only one entry is supported.
	 * @param contentId content id to check for draft
	 * @param contentOrder draft of this content order
	 * @param contentLang draft must be of this language (column content_lang)
	 * @param userId draft must be of this user (column user_id)
	 * @return user content metadata of the user's draft for content with given id, or null if no draft/content/user matched
	 */
	UserContentMetadata findContentUserDraft(@Param("contentId") String contentId,
											 @Param("contentOrder") int contentOrder,
											 @Param("contentLang") UserContentLanguage contentLang,
											 @Param("userId") int userId);
	
	/**
	 * Update the given content with the given content string and update save_dat.
	 * Only drafts are affected.
	 * @param contentUid the speicifc content to publish
	 * @param content user content to get update values from (history id, save date, content string)
	 * @return nr. of updated rows (0 or 1)
	 */
	int updateContentDraft(@Param("contentUid") long contentUid,
					  	   @Param("content") UserContent content);
	
	/**
	 * Set the given contents is_draft to false. This means publishing the content without it being approved.
	 * Important: callers must take care that the given content UID may be published by the user.
	 * @param contentUid
	 * @return nr. of updated rows (0 or 1)
	 */
	int updateContentPublish(@Param("contentUid") long contentUid);
	
	/**
	 * Find all content that has yet to be approved.
	 * Only the newest entry per content id will be returned in this list.
	 * @return list of unapproved user contents.
	 */
	List<UserContentMetadata> findAllContentUnapproved();
	
	/**
	 * Insert a new file and return its generated id.
	 * The inserted file is not active and may not be served.
	 * @param contentId content id this file belongs to
	 * @param data file contents
	 * @param type file type
	 * @param userId user who is inserting
	 * @return generated file id
	 */
	long insertContentFile(@Param("contentId") String contentId,
						   @Param("data") byte[] data,
						   @Param("type") String type,
						   @Param("userId") int userId);
	
	/**
	 * Activate the given file id to be allowed to be served to other users than user_id.
	 * @param id file id (previously inserted using {@link #insertContentFile(String, byte[], String, int)})
	 * @param contentUid the content entry which contained the given file first
	 * @param userId current user (must be the same as inserting user)
	 * @return nr. of updated rows (0 or 1)
	 */
	int updateActivateContentFile(@Param("id") long id,
								  @Param("contentUid") long contentUid,
								  @Param("userId") int userId);
	
	/**
	 * Deactivate the given file id when it has been removed from the given user content.
	 * @param id file id (previously inserted using {@link #insertContentFile(String, byte[], String, int)} and activated using {@link #updateActivateContentFile(long, long, int)})
	 * @param contentUid the content entry which removed the given file
	 * @return nr. of updated rows (0 or 1)
	 */
	int updateDeactivateContentFile(@Param("id") long id,
								    @Param("contentUid") long contentUid);
	
	/**
	 * Find a single content file by its id.
	 * @param id id of the file
	 * @param contentId content id the found file must belong to
	 * @return {@link UserContentFile}
	 */
	UserContentFile findContentFile(@Param("id") long id,
					       			@Param("contentId") String contentId);
	
}
