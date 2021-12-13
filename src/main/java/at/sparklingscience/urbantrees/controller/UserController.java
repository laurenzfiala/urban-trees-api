package at.sparklingscience.urbantrees.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import at.sparklingscience.urbantrees.controller.util.ControllerUtil;
import at.sparklingscience.urbantrees.domain.PhenologyDataset;
import at.sparklingscience.urbantrees.domain.PhenologyDatasetWithTree;
import at.sparklingscience.urbantrees.domain.Report;
import at.sparklingscience.urbantrees.domain.UserAchievements;
import at.sparklingscience.urbantrees.domain.UserContentMetadata;
import at.sparklingscience.urbantrees.domain.UserData;
import at.sparklingscience.urbantrees.domain.UserLevelAction;
import at.sparklingscience.urbantrees.domain.UserLevelActionContext;
import at.sparklingscience.urbantrees.domain.UserPermission;
import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.ClientError;
import at.sparklingscience.urbantrees.exception.UnauthorizedException;
import at.sparklingscience.urbantrees.mapper.PhenologyMapper;
import at.sparklingscience.urbantrees.mapper.UserMapper;
import at.sparklingscience.urbantrees.security.SecurityUtil;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;
import at.sparklingscience.urbantrees.service.ApplicationService;
import at.sparklingscience.urbantrees.service.AuthenticationService;
import at.sparklingscience.urbantrees.service.UserContentService;
import at.sparklingscience.urbantrees.service.UserService;

/**
 * Controller for user-related backend calls.
 * 
 * @author Laurenz Fiala
 * @since 2018/02/28
 */
@RestController
@RequestMapping("/user")
public class UserController {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

	@Autowired
    private UserMapper userMapper;

	@Autowired
    private PhenologyMapper phenologyMapper;

	@Autowired
    private AuthenticationService authService;
	
	@Autowired
    private UserService userService;

	@Autowired
    private ApplicationService appService;

	@Autowired
    private UserContentService contentService;
	
	@RequestMapping(method = RequestMethod.POST, path = "/phenology/observation/{phenologyId:\\d+}/image")
	@Transactional
	public void postPhenologyImage(
			@PathVariable int phenologyId,
			@RequestParam("file") MultipartFile image,
			Authentication auth) {
		
		LOGGER.debug("[[ POST ]] postPhenologyObservation - phenologyId: {}", phenologyId);
		
		if (image.getSize() > 5242880l) {
			throw new BadRequestException("Phenology image is too large.");
		}
		
		final String originalFilename = image.getOriginalFilename();
		if (originalFilename == null) {
			LOGGER.warn("User tried to upload file with filename: " + originalFilename);
			throw new BadRequestException("Upload filename must be set.", ClientError.PHENOLOGY_IMAGE_UPLOAD_NO_FILENAME);
		}
		
		final String fileType = image.getContentType();
		if (fileType == null || !Arrays.asList("image/jpeg", "image/png").contains(fileType)) {
			throw new BadRequestException("Upload file must be of type JPG or PNG.", ClientError.PHENOLOGY_IMAGE_UPLOAD_INVALID_TYPE);
		}
		
		try {
			this.userMapper.insertPhenologyImage(phenologyId, image.getBytes(), fileType);
		} catch (IOException e) {
			LOGGER.error("Could not upload phenology user image: " + e.getMessage(), e);
			throw new BadRequestException("Failed to retrieve image from client");
		}
		
		LOGGER.debug("[[ POST ]] postPhenologyImage |END| Successfully uploaded user image - phenologyId: {}", phenologyId);
		
		PhenologyDataset phenology = this.phenologyMapper.findPhenologyById(phenologyId);
		this.userService.increaseXp(
			UserLevelAction.PHENOLOGY_IMAGE_UPLOAD,
			phenology.getObserversUserIds(),
			new UserLevelActionContext(phenologyId, phenology.getTreeId()),
			UserPermission.PHENOLOGY_OBSERVATION,
			auth
		);
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/{userId:\\d+}/phenology")
	public List<PhenologyDatasetWithTree> getPhenologyHistory(@PathVariable int userId, Authentication auth) {
		
		final AuthenticationToken currentUser = ControllerUtil.getAuthToken(auth);
		if (!this.authService.hasUserPermission(userId, currentUser.getId(), UserPermission.PHENOLOGY_OBSERVATION_HISTORY)) {
			throw new UnauthorizedException("You are not allowed to request the given users' phenology history.", currentUser);
		}
		LOGGER.debug("[[ GET ]] getPhenologyHistory - user: {}", userId);
		
		List<PhenologyDatasetWithTree> datasets = this.phenologyMapper.findPhenologyByUserId(userId, 10);
		
		LOGGER.debug("[[ GET ]] getPhenologyHistory |END| Successfully fetched phenology history - user: {}", userId);
		return datasets;
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/{userId:\\d+}/content")
	public List<UserContentMetadata> getContentHistory(
			@PathVariable int userId,
			@RequestParam(required = false, name = "path") String pathExp,
			Authentication auth) {
		
		final AuthenticationToken currentUser = ControllerUtil.getAuthToken(auth);
		LOGGER.debug("[[ GET ]] getContentHistory - of user: {}, access by user: {}", userId, currentUser.getId());
		
		List<UserContentMetadata> history = this.contentService.getContentUserHistory(currentUser, userId, pathExp);
		
		LOGGER.debug("[[ GET ]] getContentHistory |END| Successfully fetched content history - user: {}", userId);
		return history;
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/achievements")
	public UserAchievements getAchievements(Authentication auth) {
		
		final int userId = ControllerUtil.getAuthToken(auth).getId();
		LOGGER.debug("[[ GET ]] getAchievements - user: {}", userId);
		
		UserAchievements achievements = this.userMapper.findAchievementsForUserId(userId);
		
		LOGGER.debug("[[ GET ]] getAchievements |END| Successfully fetched achievements - user: {}", userId);
		return achievements;
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/data")
	public UserData getUserData(Authentication auth) {
		
		final AuthenticationToken authToken = ControllerUtil.getAuthToken(auth);
		if (!SecurityUtil.isAdmin(authToken)) {
			return UserData.empty();
		}
		
		final int userId = authToken.getId();
		LOGGER.debug("[[ GET ]] getUserData - user: {}", userId);
		
		return this.userMapper.findUserData(userId);
		
	}
	
	@RequestMapping(method = RequestMethod.DELETE, path = "/delete")
	public void deleteUser(Authentication auth) {
		
		final int userId = ControllerUtil.getAuthToken(auth).getId();
		LOGGER.info("[[ DELETE ]] deleteUser - user: {}", userId);
		
		this.authService.deleteUser(userId);
		
		LOGGER.info("[[ DELETE ]] deleteUser |END| Successfully deleted user - user: {}", userId);
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/report")
	public List<Report> getReport(Authentication auth) {
		
		final int userId = ControllerUtil.getAuthToken(auth).getId();
		LOGGER.info("[[ PUT ]] getReport - user: {}", userId);
		
		return this.appService.getUserReports(auth);
		
	}
	
	@RequestMapping(method = RequestMethod.PUT, path = "/report")
	public void putReport(@Validated(ValidationGroups.Update.class) @RequestBody Report report,
						  Authentication auth) {
		
		final int userId = ControllerUtil.getAuthToken(auth).getId();
		LOGGER.info("[[ PUT ]] putReport - user: {}", userId);
		
		Report re = new Report();
		re.setMessage(report.getMessage());
		re.setAutoCreate(false);
		re.setUserId(userId);
		re.setReportDate(new Date());
		this.appService.report(re);
		
		LOGGER.info("[[ PUT ]] putReport |END| Successfully added report- user: {}", userId);
		
	}


}
