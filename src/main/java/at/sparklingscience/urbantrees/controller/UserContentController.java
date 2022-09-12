package at.sparklingscience.urbantrees.controller;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.MatchesPattern;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import at.sparklingscience.urbantrees.cms.CmsContent;
import at.sparklingscience.urbantrees.controller.util.ControllerUtil;
import at.sparklingscience.urbantrees.domain.ResponseFile;
import at.sparklingscience.urbantrees.domain.UserContent;
import at.sparklingscience.urbantrees.service.UserContentFileService;
import at.sparklingscience.urbantrees.service.UserContentService;

/**
 * Controller for user content-related backend calls.
 * 
 * @author Laurenz Fiala
 * @since 2020/12/10 (doc)
 */
@RestController
@RequestMapping("/content")
public class UserContentController {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserContentController.class);

	@Autowired
    private UserContentService contentService;

	@Autowired
    private UserContentFileService fileService;
	
	@RequestMapping(method = RequestMethod.GET)
	public List<UserContent> getUserContent(
			@RequestParam("path") String contentPath,
			@RequestParam("lang") String contentLanguage,
			Authentication auth) {
		
		LOGGER.debug("[[ GET ]] getUserContent - contentPath: {}, contentLang: {}", contentPath, contentLanguage);
		
		try {
			return this.contentService.getContent(
					ControllerUtil.getAuthToken(auth),
					contentPath,
					contentLanguage,
					true
			);			
		} finally {
			LOGGER.debug("[[ GET ]] getUserContent |END| - contentPath: {}, contentLang: {}", contentPath, contentLanguage);
		}
		
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public UserContent postUserContent(
			@RequestParam("path") String contentPath,
			@RequestParam("lang") String contentLanguage,
			@RequestBody @Valid CmsContent content,
			Authentication auth,
			Errors errors) {
		
		LOGGER.debug("[[ POST ]] postUserContent - contentPath: {}, language: {}", contentPath, contentLanguage);

		this.contentService.assertValid(content, errors);
		
		LOGGER.debug("[[ POST ]] postUserContentDraft - validation OK");
		
		try {
			return this.contentService.saveContent(
					ControllerUtil.getAuthToken(auth),
					contentPath,
					contentLanguage,
					false,
					content
			);
		} finally {
			LOGGER.debug("[[ POST ]] postUserContent |END| - contentPath: {}, language: {}", contentPath, contentLanguage);
		}
		
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/draft")
	public UserContent postUserContentDraft(
			@RequestParam("path") String contentPath,
			@RequestParam("lang") String contentLanguage,
			@RequestBody @Valid CmsContent content,
			Authentication auth,
			Errors errors) {
		
		LOGGER.debug("[[ POST ]] postUserContentDraft - contentPath: {}, language: {}", contentPath, contentLanguage);
		
		this.contentService.assertValid(content, errors);
		
		LOGGER.debug("[[ POST ]] postUserContentDraft - validation OK");
		
		try {
			return this.contentService.saveContent(
					ControllerUtil.getAuthToken(auth),
					contentPath,
					contentLanguage,
					true,
					content
			);			
		} finally {
			LOGGER.debug("[[ POST ]] postUserContentDraft |END| - contentPath: {}, language: {}", contentPath, contentLanguage);
		}
		
	}
	
	@RequestMapping(method = RequestMethod.DELETE, path = "/{contentUid:\\d+}")
	public void deleteUserContent(
			@PathVariable long contentUid,
			@RequestParam(name = "draftOnly", required = false, defaultValue = "false") boolean draftOnly,
			Authentication auth) {
		
		LOGGER.debug("[[ DELETE ]] deleteUserContent - contentUid: {}", contentUid);
		
		try {
			this.contentService.deleteContent(
					ControllerUtil.getAuthToken(auth),
					contentUid,
					draftOnly
			);
		} finally {
			LOGGER.debug("[[ DELETE ]] deleteUserContent |END| - contentUid: {}", contentUid);
		}
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/file/{fileUid:\\d+}")
	public ResponseEntity<Resource> getUserContentFile(
			@RequestParam("path") String contentPath,
			@RequestParam(name = "filename", required = false) @MatchesPattern("[^/]+") String filename,
			@PathVariable long fileUid,
			Authentication auth) {
		
		LOGGER.debug("[[ GET ]] getUserContentFile - contentPath: {}, fileUid: {}", contentPath, fileUid);
		
		try {
			ResponseFile file = this.fileService.getFile(
					ControllerUtil.getAuthToken(auth),
					contentPath,
					fileUid
			);
			return ResponseEntity.ok()
					.header("Content-Disposition", "attachment" + (filename == null ? "" : "; filename=\"" + filename + "\""))
					.cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
		            .contentType(MediaType.valueOf(file.getType()))
		            .body(new FileSystemResource(file.getPath()));
		} finally {
			LOGGER.debug("[[ GET ]] getUserContentFile |END| - contentPath: {}, fileUid: {}", contentPath, fileUid);
		}
		
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/file")
	public long postUserContentFile(
			@RequestParam("path") String contentPath,
			@RequestParam("file") MultipartFile file,
			Authentication auth) {
		
		LOGGER.debug("[[ POST ]] postUserContentFile - contentPath: {}", contentPath);
		
		try {
			return this.fileService.saveNewFile(
					ControllerUtil.getAuthToken(auth),
					contentPath,
					file
			);
		} finally {
			LOGGER.debug("[[ POST ]] postUserContentFile |END| - contentPath: {}", contentPath);
		}
		
	}

}
