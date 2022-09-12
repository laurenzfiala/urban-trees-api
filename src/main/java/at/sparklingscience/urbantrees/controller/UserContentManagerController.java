package at.sparklingscience.urbantrees.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import at.sparklingscience.urbantrees.controller.util.ControllerUtil;
import at.sparklingscience.urbantrees.domain.UserContent;
import at.sparklingscience.urbantrees.domain.UserContentManagerAccess;
import at.sparklingscience.urbantrees.domain.UserContentMetadata;
import at.sparklingscience.urbantrees.domain.UserContentStatus;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;
import at.sparklingscience.urbantrees.service.UserContentManagerService;

/**
 * Controller for manager-related requests on user content.
 * 
 * @author Laurenz Fiala
 * @since 2022/03/07
 */
@RestController
@RequestMapping("/manage/content")
public class UserContentManagerController {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserContentManagerController.class);

	@Autowired
    private UserContentManagerService managerService;
	
	@RequestMapping(method = RequestMethod.GET, path = "/access/viewable")
	public List<UserContentManagerAccess> getContentAccessViewable(@RequestParam(required = false) String pathExp,
																   Authentication auth) {
		
		AuthenticationToken token = ControllerUtil.getAuthToken(auth);
		LOGGER.debug("[[ GET ]] getContentAccessViewable - user: {}, pathExp: {}", token.getId(), pathExp);
		
		try {
			return this.managerService.getContentAccessViewable(pathExp, token);
		} finally {
			LOGGER.debug("[[ GET ]] getContentAccessViewable |END| - user: {}, pathExp: {}", token.getId(), pathExp);
		}
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/access/approvable")
	public List<UserContentManagerAccess> getContentAccessApprovable(@RequestParam(required = false) String pathExp,
			   														 Authentication auth) {
		
		AuthenticationToken token = ControllerUtil.getAuthToken(auth);
		LOGGER.debug("[[ GET ]] getContentAccessApprovable - user: {}, pathExp: {}", token.getId(), pathExp);
		
		try {
			return this.managerService.getContentAccessApprovable(pathExp, token);			
		} finally {
			LOGGER.debug("[[ GET ]] getContentAccessApprovable |END| - user: {}, {}", token.getId(), pathExp);
		}
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/viewable")
	public List<UserContentMetadata> getContentViewable(@RequestParam(required = true) long accessId,
												  @RequestParam(required = false) UserContentStatus status,
												  Authentication auth) {
		
		AuthenticationToken token = ControllerUtil.getAuthToken(auth);
		LOGGER.debug("[[ GET ]] getContentViewable - accessId: {}, status: {}, user: {}", accessId, status, token.getId());
		
		try {
			return this.managerService.getContentViewable(accessId, status, token);			
		} finally {
			LOGGER.debug("[[ GET ]] getContentViewable |END| - accessId: {}, status: {}, user: {}", accessId, status, token.getId());
		}
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/approvable")
	public List<UserContentMetadata> getContentApprovable(@RequestParam(required = true) long accessId,
												  @RequestParam(required = false) UserContentStatus status,
												  Authentication auth) {
		
		AuthenticationToken token = ControllerUtil.getAuthToken(auth);
		LOGGER.debug("[[ GET ]] getContentAccessApprovable - accessId: {}, status: {}, user: {}", accessId, status, token.getId());
		
		try {
			return this.managerService.getContentApprovable(accessId, status, token);			
		} finally {
			LOGGER.debug("[[ GET ]] getContentAccessApprovable |END| - accessId: {}, status: {}, user: {}", accessId, status, token.getId());
		}
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/{contentUid:\\d+}")
	public UserContent getContent(@PathVariable long contentUid,
						   		  Authentication auth) {
		
		AuthenticationToken token = ControllerUtil.getAuthToken(auth);
		LOGGER.debug("[[ GET ]] getContent - contentUid: {}, user: {}", contentUid, token.getId());
		
		try {
			return this.managerService.getContent(contentUid, token);
		} finally {
			LOGGER.debug("[[ GET ]] getContent |END| - contentUid: {}, user: {}", contentUid, token.getId());
		}
		
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/{contentUid:\\d+}/approve")
	public UserContentStatus postApproveContent(@PathVariable long contentUid,
								   Authentication auth) {
		
		AuthenticationToken token = ControllerUtil.getAuthToken(auth);
		LOGGER.debug("[[ GET ]] postApproveContent - contentUid: {}, user: {}", contentUid, token.getId());
		
		try {
			return this.managerService.updateContentStatus(contentUid, true, token);
		} finally {
			LOGGER.debug("[[ GET ]] postApproveContent |END| - contentUid: {}, user: {}", contentUid, token.getId());
		}
		
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/{contentUid:\\d+}/deny")
	public UserContentStatus postDenyContent(@PathVariable long contentUid,
								Authentication auth) {
		
		AuthenticationToken token = ControllerUtil.getAuthToken(auth);
		LOGGER.debug("[[ GET ]] postDenyContent - contentUid: {}, user: {}", contentUid, token.getId());
		
		try {
			return this.managerService.updateContentStatus(contentUid, false, token);
		} finally {
			LOGGER.debug("[[ GET ]] postDenyContent |END| - accesscontentUidId: {}, user: {}", contentUid, token.getId());
		}
		
	}

}
