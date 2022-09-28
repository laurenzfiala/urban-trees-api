package at.sparklingscience.urbantrees.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import at.sparklingscience.urbantrees.domain.User;
import at.sparklingscience.urbantrees.domain.UserContent;
import at.sparklingscience.urbantrees.domain.UserContentAccess;
import at.sparklingscience.urbantrees.domain.UserContentFile;
import at.sparklingscience.urbantrees.domain.UserContentLanguage;
import at.sparklingscience.urbantrees.domain.UserContentMetadata;
import at.sparklingscience.urbantrees.domain.UserContentSaveAmount;
import at.sparklingscience.urbantrees.domain.UserContentStatus;
import at.sparklingscience.urbantrees.domain.UserIdentity;

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
	 * Returns all content access entries.
	 */
	List<UserContentAccess> findContentAccess();
	
	/**
	 * Returns a user content access if the given content has previously been
	 * entered to the registry.
	 * @param path content path to get access entry for
	 * @return content access entry or null
	 */
	UserContentAccess findContentAccessForPath(@Param("path") String path);
	
	/**
	 * Find all content access entries that match the given path expression
	 * regex.
	 * @param pathExp regex to find content access entries
	 * @return list of matching content access entries
	 */
	List<UserContentAccess> findContentAccessByExp(@Param("pathExp") String pathExp);
	
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
	UserContentMetadata findContentMetadataById(@Param("contentUid") Long contentUid);
	
	/**
	 * TODO
	 * @param contentPath
	 * @return
	 */
	int countContentRegistryReferencesForPath(@Param("contentPath") String contentPath);
	
	/**
	 * TODO
	 * @param accessId
	 * @return
	 */
	int countContentApprovedForAccessId(@Param("accessId") long accessId);
	
	/**
	 * TODO
	 * @param accessId
	 * @return
	 */
	int countContentApprovableForAccessId(@Param("accessId") long accessId);
	
	/**
	 * Returns all content access entries that the given user (with their
	 * associated roles) may approve.
	 * @param approver approving user (usually the current user)
	 * @return all content access entries where the given user has any approval
	 * 		   permission.
	 */
	List<UserContentAccess> findContentAccessForApprover(@NonNull @Param("approver") User approver);
	
	/**
	 * TODO
	 * @param accessId
	 * @return
	 */
	List<UserContentMetadata> findContentMetadataForAccessId(@Param("accessId") Long accessId,
											 		 		 @Param("statuses") List<UserContentStatus> status);
	
	/**
	 * TODO
	 * @param accessId
	 * @return
	 */
	UserContentAccess findContentAccessById(@Param("accessId") long accessId);
	
	/**
	 * Find the current content uids for the given content path and language.
	 * Returns the same entries as {@link #findContent(String, UserContentLanguage, boolean)},
	 * but only returns the content UIDs.
	 * @param contentPath content path to search for
	 * @param contentLang content language to search for
	 * @param userId id of current user
	 * @param substituteUserDrafts true to substitute content for the users'
	 * 							   draft, if available;
	 * 							   false to only return approved content
	 * @return list of matched user content UIDs
	 */
	List<Long> findContentIdListForPath(@Param("contentPath") String contentPath,
							   	 		@Param("contentLang") UserContentLanguage contentLang,
										@Param("userId") Integer userId,
										@Param("substituteUserDrafts") boolean substituteUserDrafts);
	
	/**
	 * Find all current content entries for the given content path
	 * with given language.
	 * @param contentPath content path to search for
	 * @param contentLang content language to search for
	 * @param userId user if of current user
	 * @param substituteUserDrafts true to substitute content for the users'
	 * 							   draft, if available;
	 * 							   false to only return approved content.
	 * 							   if user is null, drafts can't be substituted
	 * @return list of matched user contents
	 */
	List<UserContent> findContent(@Param("contentPath") String contentPath,
								  @Param("contentLang") UserContentLanguage contentLang,
								  @Param("userId") Integer userId,
								  @Param("substituteUserDrafts") boolean substituteUserDrafts);
	
	/**
	 * Find a single user content by its ID.
	 * @param contentUid id to find
	 * @return user content, or null, if not found
	 */
	UserContent findContentById(@Param("contentUid") long contentUid);
	
	/**
	 * Find published user content with the given history ID.
	 * @param historyId find published content with this history id
	 * @return user content with given history id
	 */
	UserContent findContentForHistoryId(@Param("historyId") long historyId);
	
	
	/**
	 * Find given content by its' uid and all of its' history entries.
	 * @param contentUid content entry id
	 * @return list of content history starting with content entry with given id
	 */
	List<UserContent> findContentHistory(
			@Param("contentUid") long contentUid
			);
	
	
	/**
	 * Find all past user content edits (one per content id).
	 * @param userId user for whom to find the history for
	 * @param contentPathExp only find content which matches the given path
	 * 						 expression
	 * @param limit max amount of history entries
	 * @return list of cms content history for given user
	 */
	List<UserContentMetadata> findContentUserHistory(
			@Param("userId") int userId,
			@Param("contentPathExp") String contentPathExp,
			@Param("limit") int limit
			);
	
	/**
	 * Insert a new content to content_registry. If the given path is already
	 * registered, the error is ignored.
	 * @param contentPath content path to register
	 * @param access list of applicable content access entries to save for the
	 * 				 content
	 */
	void registerContent(@Param("contentPath") String contentPath,
						 @Param("access") List<UserContentAccess> access);
	
	/**
	 * TODO
	 * @param contentPath
	 */
	void deregisterContent(@Nullable @Param("contentPath") String contentPath);
	
	/**
	 * Insert a new content entry into the DB and update the given contents id.
	 * @param content content to insert. id field is updated on insert.
	 * @return number of rows inserted (always 1).
	 */
	int insertContent(@Param("c") UserContent content);
	
	/**
	 * Update an existing content in the DB.
	 * This only updates fields of {@link UserContent} that may be
	 * changed.
	 * @param content content to set
	 * @return number of rows updated (<= 1).
	 */
	int updateContent(@Param("c") UserContent content);
	
	/**
	 * Update the content entry with given ID to the given status.
	 * @param contentUid content entry to update
	 * @param approveUserId user ID of approver
	 * @param status status to set
	 * @return rows updated (0 or 1)
	 */
	int updateContentStatus(@Param("contentUid") long contentUid,
							@Param("approveUserId") long approveUserId,
							@Param("status") UserContentStatus status);
	
	/**
	 * Update references of next and previous contents to the given IDs.
	 * @param content content used to get previous and next content (to update)
	 * @param prevNextId next ID at content.previousId is updated to this
	 * @param nextPrevId previous ID at content.nextId is updated to this
	 * @return no. of updated rows
	 */
	int stitchContent(@Param("c") UserContentMetadata content,
					  @Param("prevNextId") Long prevNextId,
					  @Param("nextPrevId") Long nextPrevId);
	
	/**
	 * Find a previously stored draft for the given user/content/lang/order combination.
	 * Since only one draft per content path/history id per user can be stored, only one entry is supported.
	 * 
	 * TODO redo doc
	 * @param contentPath content path to check for draft
	 * @param historyId given history id from cms content (may reference approved content or draft)
	 * @param prevId given previous id from cms content (may reference approved content or draft)
	 * @param nextId given next id from cms content (may reference approved content or draft)
	 * @param contentLang draft must be of this language (column content_lang)
	 * @param userId draft must be of this user (column user_id)
	 * @return user content metadata of the user's draft for content with given id, or null if no draft/content/user matched
	 */
	UserContentMetadata findContentUserDraft(@Param("contentPath") String contentPath,
											 @Param("historyId") Long historyId,
											 @Param("prevId") Long prevId,
											 @Param("nextId") Long nextId,
											 @Param("contentLang") UserContentLanguage contentLang,
											 @Param("userId") int userId);
	
	/**
	 * Delete the content with given ID from the DB.
	 * This actually deletes the entry.
	 * @param id draft content UID
	 * @return amount of deleted rows (0 or 1)
	 */
	int deleteContent(@Param("id") long id);
	
	/**
	 * Delete the draft with given ID from the DB.
	 * This actually deletes the entry.
	 * @param id draft content UID
	 * @return amount of deleted rows (0 or 1)
	 */
	int deleteContentUserDraft(@Param("id") long id);
	
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
	 * Set the given contents' status to AWAITING_APPROVAL.
	 * This means publishing the content without it being approved.
	 * Important: callers must take care that the given content UID may be published by the user.
	 * @param contentUid content entry to be updated
	 * @return nr. of updated rows (0 or 1)
	 */
	int updateContentPublish(@Param("contentUid") long contentUid);
	
	/**
	 * Set the given contents' status to DELETED and stitch together
	 * previous and next content entries.
	 * This means the content can in no way be viewed or edited anymore.
	 * Important: callers must take care that the given content UID may be deleted by the user.
	 * @param contentUid content entry to be updated
	 * @return nr. of updated rows (0 or 1)
	 */
	int updateContentDelete(@Param("contentUid") long contentUid);
	
	/**
	 * Insert a new file and return its generated id.
	 * The inserted file is not active and may not be served.
	 * @param contentPath content path this file belongs to
	 * @param file user content file object used to get content type, path, user id.
	 */
	void insertContentFile(@Param("contentPath") String contentPath,
						   @Param("file") UserContentFile file);
	
	/**
	 * Activate the given file id to be allowed to be served to other users than user_id.
	 * @param id file id (previously inserted using {@link #insertContentFile(String, byte[], String, int)})
	 * @param contentUid the content entry which contained the given file first
	 * @param user current user (must be the same as inserting user)
	 * @return nr. of updated rows (0 or 1)
	 */
	int updateActivateContentFile(@Param("id") long id,
								  @Param("contentUid") long contentUid,
								  @Param("user") UserIdentity user);
	
	/**
	 * Deactivate the given file id when it has been removed from the given user content.
	 * @param id file id (previously inserted using {@link #insertContentFile(String, byte[], String, int)} and activated using {@link #updateActivateContentFile(long, long, int)})
	 * @param contentUid the content entry which removed the given file
	 * @return nr. of updated rows (0 or 1)
	 */
	int updateDeactivateContentFile(@Param("id") long id,
								    @Param("contentUid") long contentUid);
	

	/**
	 * Fully delete a content file entry from the database. Does not check for
	 * activation state.
	 * Note that this does not take care of the saved file on the filesystem.
	 * @param id file id (previously inserted using {@link #insertContentFile(String, byte[], String, int)})
	 * @return amount of deleted rows (0 or 1)
	 */
	int deleteContentFile(@Param("id") long id);
	
	/**
	 * Find a single content file by its id.
	 * @param id id of the file
	 * @return {@link UserContentFile}
	 */
	UserContentFile findContentFile(@Param("id") long id);
	
	/**
	 * Find all content files given the ids in the list.
	 * @param ids list of file IDs (may not be empty or null)
	 * @return list of files
	 */
	List<UserContentFile> findContentFiles(@Param("ids") List<Long> ids);
	
	/**
	 * Find all files that are active in the context of the given content entry.
	 * This also includes files that were activated in one of the contents'
	 * changes (history).
	 * @param contentUid contents' UID to check
	 * @return list of all active file IDs for the given content
	 */
	List<Long> findActiveContentFilesForContentUid(@Param("contentUid") long contentUid);

	/**
	 * Find all user uploaded files for the given content path that have never
	 * been activated.
	 * @param contentPath content path
	 * @param user user identity (may be null for anonymous)
	 * @return list of files uploaded but never activated
	 */
	List<UserContentFile> findInactiveContentFilesForContentPathAndUser(@Param("contentPath") String contentPath,
																		@Param("user") UserIdentity user);
	
	
}
