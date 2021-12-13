package at.sparklingscience.urbantrees.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import at.sparklingscience.urbantrees.domain.UserContent;
import at.sparklingscience.urbantrees.domain.UserContentAccess;
import at.sparklingscience.urbantrees.domain.UserContentFile;
import at.sparklingscience.urbantrees.domain.UserContentLanguage;
import at.sparklingscience.urbantrees.domain.UserContentMetadata;
import at.sparklingscience.urbantrees.domain.UserContentSaveAmount;
import at.sparklingscience.urbantrees.domain.UserContentStatus;

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
	 * Find all content access entries that match the given path expression
	 * regex.
	 * @param pathExp regex to find content access entries
	 * @return list of matching content access entries
	 */
	List<UserContentAccess> findContentAccess(@Param("pathExp") String pathExp);
	
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
										@Param("userId") int userId,
										@Param("substituteUserDrafts") boolean substituteUserDrafts);
	
	/**
	 * Find all current content entries for the given content path
	 * with given language.
	 * @param contentPath content path to search for
	 * @param contentLang content language to search for
	 * @param userId id of current user
	 * @param substituteUserDrafts true to substitute content for the users'
	 * 							   draft, if available;
	 * 							   false to only return approved content
	 * @return list of matched user contents
	 */
	List<UserContent> findContent(@Param("contentPath") String contentPath,
								  @Param("contentLang") UserContentLanguage contentLang,
								  @Param("userId") int userId,
								  @Param("substituteUserDrafts") boolean substituteUserDrafts);
	
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
	 * Approve the given content by given user.
	 * @param contentUid internal id of content (not content id)
	 * @param userId user id of approval user
	 * @return nr. of rows updated. Note: must be 1. If it is 0 and content exists,
	 * 		   the content may have already been approved.
	 */
	int approveContentById(@Param("contentUid") long contentUid,
						   @Param("userId") int userId);
	
	/**
	 * Insert a new content to content_registry. If the given path is already
	 * registered, the error is ignored.
	 * @param contentPath content path to register
	 */
	void registerContent(@Param("contentPath") String contentPath);
	
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
	 * @param status status to set
	 * @return rows updated (0 or 1)
	 */
	int updateContentStatus(@Param("contentUid") long contentUid,
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
	 * @param contentLang draft must be of this language (column content_lang)
	 * @param userId draft must be of this user (column user_id)
	 * @return user content metadata of the user's draft for content with given id, or null if no draft/content/user matched
	 */
	UserContentMetadata findContentUserDraft(@Param("contentPath") String contentPath,
											 @Param("historyId") long historyId,
											 @Param("contentLang") UserContentLanguage contentLang,
											 @Param("userId") int userId);
	
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
	 * @param userId current user (must be the same as inserting user)
	 * @return nr. of updated rows (0 or 1)
	 */
	int updateActivateContentFile(@Param("id") long id,
								  @Param("contentUid") long contentUid,
								  @Param("userId") Integer userId);
	
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
	 * @param contentPath content path the found file must belong to
	 * @return {@link UserContentFile}
	 */
	UserContentFile findContentFile(@Param("id") long id,
					       			@Param("contentPath") String contentPath);
	
	/**
	 * Find all files that are active in the context of the given content entry.
	 * This also includes files that were activated in one of the contents'
	 * changes (history).
	 * @param contentUid contents' UID to check
	 * @return list of all active files for the given content
	 */
	List<UserContentFile> findActiveContentFilesForContentUid(@Param("contentUid") long contentUid);
	
}
