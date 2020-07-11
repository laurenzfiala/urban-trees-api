package at.sparklingscience.urbantrees.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import at.sparklingscience.urbantrees.controller.util.ControllerUtil;
import at.sparklingscience.urbantrees.domain.UserContent;
import at.sparklingscience.urbantrees.domain.UserContentTag;
import at.sparklingscience.urbantrees.service.UserContentService;

/**
 * Controller for user-related backend calls.
 * 
 * @author Laurenz Fiala
 * @since 2018/02/28
 */
@RestController
@RequestMapping("/content")
public class UserContentController {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserContentController.class);

	@Autowired
    private UserContentService contentService;
	
	@RequestMapping(method = RequestMethod.GET, path = "/{contentId:\\d+}")
	public UserContent getUserContent(
			@PathVariable int contentId,
			Authentication auth) {
		
		LOGGER.debug("[[ GET ]] getUserContent - contentId: {}", contentId);
		
		try {
			return this.contentService.getContent(ControllerUtil.getAuthToken(auth), contentId);			
		} finally {
			LOGGER.debug("[[ GET ]] getUserContent - contentId: {}", contentId);
		}
		
	}
	

	
	@RequestMapping(method = RequestMethod.GET, path = "/{contentTag:[A-Z]+}")
	public List<UserContent> getUserContent(
			@PathVariable UserContentTag contentTag,
			Authentication auth) {
		
		LOGGER.debug("[[ GET ]] getUserContent - contentTag: {}", contentTag);
		
		try {
			return this.contentService.getContent(ControllerUtil.getAuthToken(auth), contentTag);			
		} finally {
			LOGGER.debug("[[ GET ]] getUserContent - contentTag: {}", contentTag);
		}
		
	}

}
