package at.sparklingscience.urbantrees.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import at.sparklingscience.urbantrees.cms.CmsContent;
import at.sparklingscience.urbantrees.controller.util.ControllerUtil;
import at.sparklingscience.urbantrees.domain.UserContent;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;
import at.sparklingscience.urbantrees.service.UserContentService;

/**
 * Controller for user-related backend calls.
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
	
	@RequestMapping(method = RequestMethod.GET, path = "/{contentId}/{contentLang}")
	public List<UserContent> getUserContent(
			@PathVariable String contentId,
			@PathVariable String contentLang,
			Authentication auth) {
		
		LOGGER.debug("[[ GET ]] getUserContent - contentId: {}, contentLang: {}", contentId, contentLang);
		
		try {
			return this.contentService.getContent(ControllerUtil.getAuthToken(auth), contentId, contentLang, false);			
		} finally {
			LOGGER.debug("[[ GET ]] getUserContent |END| - contentId: {}, contentLang: {}", contentId, contentLang);
		}
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/user/{contentId}/{contentLang}")
	public List<UserContent> getUserContentForUser(
			@PathVariable String contentId,
			@PathVariable String contentLang,
			Authentication auth) {
		
		AuthenticationToken authToken = ControllerUtil.getAuthToken(auth);
		LOGGER.debug("[[ GET ]] getUserContentForUser - contentId: {}, contentLang: {}, userId: {}", contentId, contentLang, authToken.getId());
		
		try {
			return this.contentService.getContent(authToken, contentId, contentLang, true);			
		} finally {
			LOGGER.debug("[[ GET ]] getUserContentForUser |END| - contentId: {}, contentLang: {}, userId: {}", contentId, contentLang, authToken.getId());
		}
		
	}

	@RequestMapping(method = RequestMethod.POST, path = "/{contentId}/{contentOrder:\\d+}/{contentLanguage}")
	public UserContent postUserContent(
			@PathVariable String contentId,
			@PathVariable int contentOrder,
			@PathVariable String contentLanguage,
			@RequestBody @Valid CmsContent content,
			Authentication auth,
			Errors errors) {
		
		this.contentService.assertValid(content, errors);
		
		LOGGER.debug("[[ POST ]] postUserContent - contentId: {}, order: {}, language: {}", contentId, contentOrder, contentLanguage);
		
		try {
			return this.contentService.saveContent(
					ControllerUtil.getAuthToken(auth),
					contentId,
					contentOrder,
					contentLanguage,
					false,
					content
			);			
		} finally {
			LOGGER.debug("[[ POST ]] postUserContent |END| - contentId: {}, order: {}, language: {}", contentId, contentOrder, contentLanguage);
		}
		
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/{contentId}/{contentOrder:\\d+}/{contentLanguage}/draft")
	public UserContent postUserContentDraft(
			@PathVariable String contentId,
			@PathVariable int contentOrder,
			@PathVariable String contentLanguage,
			@RequestBody @Valid CmsContent content,
			Authentication auth,
			Errors errors) {
		
		LOGGER.debug("[[ POST ]] postUserContentDraft - contentId: {}, order: {}, language: {}", contentId, contentOrder, contentLanguage);
		
		this.contentService.assertValid(content, errors);
		
		LOGGER.debug("[[ POST ]] postUserContentDraft - validation OK");
		
		try {
			return this.contentService.saveContent(
					ControllerUtil.getAuthToken(auth),
					contentId,
					contentOrder,
					contentLanguage,
					true,
					content
			);			
		} finally {
			LOGGER.debug("[[ POST ]] postUserContentDraft |END| - contentId: {}, order: {}, language: {}", contentId, contentOrder, contentLanguage);
		}
		
	}

}
