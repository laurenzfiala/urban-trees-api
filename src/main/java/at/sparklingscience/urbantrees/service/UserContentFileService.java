package at.sparklingscience.urbantrees.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.sparklingscience.urbantrees.cms.CmsContent;
import at.sparklingscience.urbantrees.cms.UserContentConfiguration;
import at.sparklingscience.urbantrees.cms.component.FileComponent;
import at.sparklingscience.urbantrees.domain.ResponseFile;
import at.sparklingscience.urbantrees.domain.UserContent;
import at.sparklingscience.urbantrees.domain.UserContentAccess;
import at.sparklingscience.urbantrees.domain.UserContentFile;
import at.sparklingscience.urbantrees.domain.UserContentMetadata;
import at.sparklingscience.urbantrees.domain.UserContentStatus;
import at.sparklingscience.urbantrees.domain.UserIdentity;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.ClientError;
import at.sparklingscience.urbantrees.exception.InternalException;
import at.sparklingscience.urbantrees.exception.NotFoundException;
import at.sparklingscience.urbantrees.exception.UnauthorizedException;
import at.sparklingscience.urbantrees.mapper.UserContentMapper;
import at.sparklingscience.urbantrees.security.SecurityUtil;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;

/**
 * Service for actions on user content files.
 * 
 * @author Laurenz Fiala
 * @since 2022/02/20
 */
@Service
public class UserContentFileService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserContentFileService.class);
	
	@Autowired
    private UserContentAccessService accessService;
	
	@Autowired
    private UserContentMapper contentMapper;

	@Autowired
	@Qualifier("jsonCmsObjectMapper")
	private ObjectMapper jsonObjectMapper;
	
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
		
		this.accessService.assertViewPermission(authToken, contentPath);
		
		UserContentFile file = this.contentMapper.findContentFile(fileId);
		if (!contentPath.equals(file.getContentPath())) {
			throw new NotFoundException("Given file does not exist for content path '" + contentPath + "'.");
		}
		UserContentMetadata metadata = this.contentMapper.findContentMetadataById(file.getActivateContentUid());
		UserIdentity editUser = metadata.getUser();
		
		// same user
		if (authToken != null && file.getUserId() == authToken.getId()) {
			return file;
			
		// active file and approved content
		} else if (file.isActive() && file.getActivateContentUid() != null && metadata.getStatus() == UserContentStatus.APPROVED) {
			return file;
			
		// current user is approver
		} else if (this.accessService.hasApprovalPermission(authToken, editUser, contentPath)) {
			return file;
		}
		
		throw new UnauthorizedException("Unauthorized access to file", authToken);
		
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
		
		this.accessService.assertViewPermission(authToken, contentPath);
		
		UserContentFile file = this.contentMapper.findContentFile(fileUid);
		if (file == null || !contentPath.equals(file.getContentPath())) {
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
		
		this.accessService.assertEditPermission(authToken, contentPath);
		
		final Path absoluteRoot = UserContentConfiguration.FILE_ROOT.toAbsolutePath();
		Path targetPath = absoluteRoot.resolve(UUID.randomUUID().toString());
		
		try {
			
			File targetFile = targetPath.toFile();
			targetFile.createNewFile();
			
			try (InputStream is = file.getInputStream();
				 FileOutputStream os = new FileOutputStream(targetFile)) {
				this.writeWithoutFileMetadata(file.getContentType(), is, os);				
			}			
			
			final UserContentFile insertFile = new UserContentFile(
					contentPath,
					absoluteRoot.relativize(targetPath).toString(),
					file.getContentType(),
					authToken == null ? null : authToken.getId()
			);
			
			this.contentMapper.registerContent(contentPath, null);
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
	 * Holds custom logic for all file types supported for metadata removal.
	 * All exceptions raised are passed up so the new user file can be safely
	 * deleted again. The caller must make sure to flush/close the given
	 * streams.
	 * Currently, only JPG/JPEG iages are supported.
	 * @param contentType content type of the file given by "is"
	 * @param is file input stream
	 * @param os target file output stream (where it should be stored)
	 * @throws Exception any exception that occurs during metadata removal
	 */
	private void writeWithoutFileMetadata(String contentType,
										  InputStream is,
										  OutputStream os) throws Exception {

		LOGGER.debug("Checking whether file metadata for content type {} can be removed.", contentType);
		
		switch (contentType) {
		case MediaType.IMAGE_JPEG_VALUE:
			new ExifRewriter().removeExifMetadata(is, os);
			break;

		default:
			is.transferTo(os);
			LOGGER.debug("No file metadata removed.");
			return;
		}
		

		LOGGER.debug("Removed file metadata for content type {}.", contentType);
		
	}
	
	/**
	 * Set the files within the given user content to active, so they
	 * can be viewed not only by the uploader. All other files previously
	 * referenced by APPROVED content is deactivated.
	 * Important:	Make sure the user of given authToken has permission
	 * 				to do so.
	 * TODO dont deactivate all, then activate all; use diff
	 * @param content newly updated user content holding content and metadata
	 * @param cmsContent new CMS content to be checked for files
	 * @param user user with permission to publish files for the given user
	 * 			   content
	 * @throws BadRequestException if the given CMS content contains any
	 * 							   invalid file references.
	 */
	public void publishContentUpdateFiles(@NonNull UserContent content,
										  @NonNull CmsContent cmsContent,
										  @Nullable UserIdentity user) throws BadRequestException{
		
		List<Long> fileIds = FileComponent.findUidsForContent(cmsContent);
		if (content.getHistoryId() != null) {
			this.deactivateFiles(content.getId(), List.of(), fileIds);
		}
		if (fileIds.size() == 0) {
			return;
		}
		
		List<UserContentFile> files = this.contentMapper.findContentFiles(fileIds);
		
		files.stream().forEach(file -> {
			if (file.isActive()) return;
			int updatedRows = this.contentMapper.updateActivateContentFile(
					file.getId(),
					content.getId(),
					user
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
	 * Deactivate all files, except the given ones, that are currently active
	 * for any content entry in the history chain of the given content.
	 * @param contentUid content UID to check for files
	 * @param includeFiles file IDs to deactivate in any case
	 * @param excludeFiles file IDs not to deactivate
	 */
	public void deactivateFiles(long contentUid,
								@NonNull List<Long> includeFiles,
								@NonNull List<Long> excludeFiles) {
		
		List<Long> previousFiles = this.contentMapper.findActiveContentFilesForContentUid(contentUid);
		previousFiles.addAll(includeFiles);
		previousFiles.stream()
			.filter(id -> !excludeFiles.contains(id))
			.forEach(id -> {
				this.contentMapper.updateDeactivateContentFile(
						id,
						contentUid
						);
			});
		
	}
	
	/**
	 * Delete all files, except the given ones, that are currently active
	 * for any content entry in the history chain of the given content.
	 * @param contentUid content UID to check for files
	 * @param includeFiles file IDs to delete in any case
	 * @param excludeFiles file IDs not to deactivate
	 */
	public void deleteFiles(long contentUid, List<Long> includeFiles, List<Long> excludeFiles) {
		
		List<Long> previousFiles = this.contentMapper.findActiveContentFilesForContentUid(contentUid);
		previousFiles.addAll(includeFiles);
		previousFiles.stream()
			.filter(id -> !excludeFiles.contains(id))
			.forEach(id -> {
				UserContentFile file = this.contentMapper.findContentFile(id);
				if (file != null) this.deleteFile(file);
			});
		
	}
	
	/**
	 * TODO
	 * @param content
	 * @throws InternalException
	 */
	public void cleanUpFiles(@NonNull UserContent content) throws InternalException {
		
		CmsContent cmsContent;
		try {
			cmsContent = this.jsonObjectMapper.readValue(content.getContent(), CmsContent.class);
		} catch (JsonProcessingException e) {
			throw new InternalException("Can't deserialize content.");
		}
		UserContent baseContent = null;
		if (content.getHistoryId() != null) {
			baseContent = this.contentMapper.findContentById(content.getHistoryId());			
		}
		
		List<UserContentAccess> access = this.accessService.getContentAccess(content.getContentPath());
		boolean keepHistory = this.accessService.shouldKeepHistory(access);
		
		this.cleanUpFiles(content, cmsContent, baseContent, keepHistory);
	
	}
	
	/**
	 * TODO doc
	 * Call as last operation when content is saved.
	 * This method purges inactive files for the user who created the given
	 * content. If keepHistory is false and the given content is APPROVED, this
	 * method also purges files that are no longer referenced in the new content
	 * (compared to the immediate predecessor i.e. "historyId").
	 * @param content
	 * @param cmsContent
	 * @param keepHistory
	 * @throws JsonProcessingException 
	 * @throws JsonMappingException 
	 */
	public void cleanUpFiles(@NonNull UserContentMetadata content,
							 @NonNull CmsContent cmsContent,
							 @Nullable UserContent baseContent,
							 boolean keepHistory) throws InternalException {
		
		List<Long> filesInCmsContent = FileComponent.findUidsForContent(cmsContent);
		this.purgeInactiveFilesForUser(content.getContentPath(), content.getUser(), filesInCmsContent);
		
		if (baseContent != null) {
			CmsContent previousCmsContent;
			try {
				previousCmsContent = this.jsonObjectMapper.readValue(baseContent.getContent(), CmsContent.class);
			} catch (JsonProcessingException e) {
				throw new InternalException("Can't deserialize base content.");
			}
			List<Long> filesInPreviousCmsContent = FileComponent.findUidsForContent(previousCmsContent);
			
			if (content.getStatus() == UserContentStatus.APPROVED && !keepHistory) {
				this.deleteFiles(content.getId(), filesInPreviousCmsContent, filesInCmsContent);
			}			
		}
		
	}
	
	/**
	 * Fully delete all files that have never been activated but are registered
	 * for the given content path (DB and filesystem).
	 * Only deletes files that have been uploaded by the given user.
	 * @param contentPath content path for which inactive files should be purged
	 * @param user user of whom inactive files should be purged
	 */
	private void purgeInactiveFilesForUser(String contentPath, UserIdentity user, List<Long> excludingFileUids) {
		
		final Path absoluteRoot = UserContentConfiguration.FILE_ROOT.toAbsolutePath();
		
		List<UserContentFile> inactiveFiles = this.contentMapper.findInactiveContentFilesForContentPathAndUser(contentPath, user);
		
		inactiveFiles.stream()
			.filter(f -> !excludingFileUids.contains(f.getId()))
			.forEach(f -> {
				Path targetPath = absoluteRoot.resolve(f.getPath());
				targetPath.toFile().delete();
				this.contentMapper.deleteContentFile(f.getId());
			});
		
	}
	
	/**
	 * Fully deletes the given file, from the database and the filesystem.
	 * @param file file
	 */
	private void deleteFile(@NonNull UserContentFile file) {
		
		final Path absoluteRoot = UserContentConfiguration.FILE_ROOT.toAbsolutePath();
		Path targetPath = absoluteRoot.resolve(file.getPath());
		
		targetPath.toFile().delete();
		this.contentMapper.deleteContentFile(file.getId());
		
	}

}
