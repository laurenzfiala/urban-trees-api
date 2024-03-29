package at.sparklingscience.urbantrees.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import at.sparklingscience.urbantrees.controller.util.ControllerUtil;
import at.sparklingscience.urbantrees.controller.util.Timespan;
import at.sparklingscience.urbantrees.domain.Beacon;
import at.sparklingscience.urbantrees.domain.BeaconLog;
import at.sparklingscience.urbantrees.domain.BeaconLogSeverity;
import at.sparklingscience.urbantrees.domain.BeaconLogType;
import at.sparklingscience.urbantrees.domain.BeaconSettings;
import at.sparklingscience.urbantrees.domain.BeaconStatus;
import at.sparklingscience.urbantrees.domain.City;
import at.sparklingscience.urbantrees.domain.PhenologyObservationType;
import at.sparklingscience.urbantrees.domain.Report;
import at.sparklingscience.urbantrees.domain.Role;
import at.sparklingscience.urbantrees.domain.SearchResult;
import at.sparklingscience.urbantrees.domain.Tree;
import at.sparklingscience.urbantrees.domain.UserBulkAction;
import at.sparklingscience.urbantrees.domain.UserBulkActionData;
import at.sparklingscience.urbantrees.domain.UserCreation;
import at.sparklingscience.urbantrees.domain.UserLight;
import at.sparklingscience.urbantrees.domain.ui.Announcement;
import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.ClientError;
import at.sparklingscience.urbantrees.exception.DuplicateUsernameException;
import at.sparklingscience.urbantrees.exception.InternalException;
import at.sparklingscience.urbantrees.mapper.ApplicationMapper;
import at.sparklingscience.urbantrees.mapper.BeaconMapper;
import at.sparklingscience.urbantrees.mapper.PhenologyMapper;
import at.sparklingscience.urbantrees.mapper.TreeMapper;
import at.sparklingscience.urbantrees.mapper.UiMapper;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;
import at.sparklingscience.urbantrees.service.AdminService;
import at.sparklingscience.urbantrees.service.ApplicationService;

@RestController
@RequestMapping("/admin")
public class AdminController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
	
	@Autowired
	private TreeMapper treeMapper;
	
	@Autowired
	private BeaconMapper beaconMapper;
	
	@Autowired
	private PhenologyMapper phenologyMapper;
	
	@Autowired
	private UiMapper uiMapper;
	
	@Autowired
	private ApplicationMapper appMapper;
	
	@Autowired
	private AdminService adminService;
	
	@Autowired
	private ApplicationService appService;
	
	@Value("${at.sparklingscience.urbantrees.dateFormatPattern}")
	private String dateFormatPattern;
	
	@Transactional
	@RequestMapping(method = RequestMethod.POST, path = "/city")
	public City postCity(@Validated(ValidationGroups.Update.class) @RequestBody City city, Authentication auth) {
		
		LOGGER.info("[[ POST ]] postCity - city: {}", city.getName());
		
		final int userId = ControllerUtil.getAuthToken(auth).getId();
		try {
			this.treeMapper.insertCity(city, String.valueOf(userId));
		} catch (DuplicateKeyException ex) {
			LOGGER.warn("Admin tried to enter duplicate city: {}", ex.getMessage(), ex);
			throw new BadRequestException("There is already a city with given name.", ClientError.CITY_DUPLICATE);
		} catch (Throwable t) {
			LOGGER.error("Internal excetion during postCity: {}", t.getMessage(), t);
			throw new InternalException("Internal error encountered while adding city.", ClientError.CITY_INTERNAL_ERROR);
		}
		
		LOGGER.info("[[ POST ]] postCity |END| - city: {}, inserted city id: {}", city.getName(), city.getId());
		
		return city;
		
	}
	
	@Transactional
	@RequestMapping(method = RequestMethod.POST, path = "/beacon")
	public Beacon postNewBeacon(@Validated(ValidationGroups.Update.class) @RequestBody Beacon beacon, Authentication auth) {
		
		LOGGER.info("[[ POST ]] postNewBeacon - deviceId: {}", beacon.getDeviceId());
		
		// initialize default values
		beacon.setStatus(BeaconStatus.INITIAL);
		
		final int userId = ControllerUtil.getAuthToken(auth).getId();
		try {
			if (beacon.getTree() == null || beacon.getTree().getLocation().getId() != beacon.getLocation().getId()) { // new location entry
				this.treeMapper.insertLocation(beacon.getLocation(), String.valueOf(userId));				
			}
			this.beaconMapper.insertBeacon(beacon, String.valueOf(userId));
			this.beaconMapper.insertBeaconSettings(beacon.getId(), beacon.getSettings(), String.valueOf(userId));
		} catch (DuplicateKeyException ex) {
			LOGGER.warn("Admin tried to enter duplicate beacon: {}", ex.getMessage(), ex);
			throw new BadRequestException("There is already a beacon with given deviceId or same address.", ClientError.BEACON_DUPLICATE);
		} catch (Throwable t) {
			LOGGER.error("Internal exception during postBeacon: {}", t.getMessage(), t);
			throw new InternalException("Internal error encountered while adding beacon", ClientError.BEACON_INTERNAL_ERROR, t);
		}
		
		LOGGER.info("[[ POST ]] postNewBeacon |END| - deviceId: {}, inserted beacon id: {}", beacon.getDeviceId(), beacon.getId());
		
		return beacon;
		
	}
	
	@Transactional
	@RequestMapping(method = RequestMethod.POST, path = "/beacon/{beaconId:\\d+}")
	public Beacon postBeaconUpdate(
			@PathVariable("beaconId") int beaconId,
			@Validated(ValidationGroups.Update.class) @RequestBody Beacon beacon,
			Authentication auth) {
		
		final int userId = ControllerUtil.getAuthToken(auth).getId();
		LOGGER.info("[[ POST ]] postBeaconUpdate - tree id: {}, admin uid: {}", beaconId, userId);
		
		try {
			
			final Beacon oldBeacon = this.beaconMapper.findBeaconById(beaconId);
			oldBeacon.setSettings(this.beaconMapper.findLatestBeaconSettingsByBeaconId(beaconId));
			
			if (beacon.getLocation().getId() == 0) { // new location entry (beacon loc was separated from tree)
				this.treeMapper.insertLocation(beacon.getLocation(), String.valueOf(userId));
				this.beaconMapper.updateBeacon(beacon, String.valueOf(userId));
			} else if (beacon.getTree() != null && beacon.getTree().getLocation().getId() == beacon.getLocation().getId() &&
					   oldBeacon.getLocation().getId() != beacon.getLocation().getId()) { // beacon attached to tree
				this.beaconMapper.updateBeacon(beacon, String.valueOf(userId));
				if (oldBeacon.getTree() == null) { // previously independent beacon was attached to tree
					this.treeMapper.deleteLocation(oldBeacon.getLocation().getId());
				} else if (oldBeacon.getLocation().getId() != oldBeacon.getTree().getId()) { // previously unlocked beacon location was locked
					this.treeMapper.deleteLocation(oldBeacon.getLocation().getId());
				}
			} else if (beacon.getTree() == null ||
					   beacon.getLocation().getId() != beacon.getTree().getLocation().getId()) { // update beacon-specific loc
				this.treeMapper.updateLocation(beacon.getLocation(), String.valueOf(userId));
				this.beaconMapper.updateBeacon(beacon, String.valueOf(userId));
			} else { // loc unchanged
				this.beaconMapper.updateBeacon(beacon, String.valueOf(userId));
			}
			
			BeaconSettings newSettings = oldBeacon.getSettings();
			newSettings.setPin(beacon.getSettings().getPin());
			this.beaconMapper.updateBeaconSettings(newSettings, String.valueOf(userId));
			
		} catch (Throwable t) {
			LOGGER.error("Internal excetion during postBeaconUpdate: {}", t.getMessage(), t);
			throw new BadRequestException("Internal error encountered while updating beacon.", ClientError.BEACON_UPDATE_FAILED);
		}
		
		LOGGER.info("[[ POST ]] postBeaconUpdate |END| - beacon id: {}, admin uid: {}", beaconId, userId);
		
		return beacon;
		
	}

	@Transactional
	@RequestMapping(method = RequestMethod.POST, path = "/tree")
	public Tree postTree(@Validated(ValidationGroups.Update.class) @RequestBody Tree tree, Authentication auth) {
		
		LOGGER.info("[[ POST ]] postTree");
		
		final String name = auth.getName();
		try {
			this.treeMapper.insertLocation(tree.getLocation(), name);
			this.treeMapper.insertTree(tree, name);
			this.treeMapper.insertTreeAge(tree, name);
		} catch (Throwable t) {
			LOGGER.error("Internal excetion during postTree: {}", t.getMessage(), t);
			throw new BadRequestException("Internal error encountered while adding tree.", ClientError.TREE_INSERT_FAILED);
		}
		
		LOGGER.info("[[ POST ]] postTree |END| - inserted tree id: {}", tree.getId());
		
		return tree;
		
	}
	
	@Transactional
	@RequestMapping(method = RequestMethod.POST, path = "/tree/{treeId:\\d+}")
	public Tree postTreeUpdate(
			@PathVariable("treeId") int treeId,
			@Validated(ValidationGroups.Update.class) @RequestBody Tree tree,
			Authentication auth) {
		
		LOGGER.info("[[ POST ]] postTreeUpdate - tree id: {}", treeId);
		
		try {
			this.treeMapper.updateTree(tree, auth.getName());
		} catch (Throwable t) {
			LOGGER.error("Internal excetion during postTreeUpdate: {}", t.getMessage(), t);
			throw new BadRequestException("Internal error encountered while updating tree.", ClientError.TREE_UPDATE_FAILED);
		}
		
		LOGGER.info("[[ POST ]] postTreeUpdate |END| - tree id: {}", treeId);
		
		return tree;
		
	}

	@Transactional
	@RequestMapping(method = RequestMethod.DELETE, path = "/beacon/{beaconId:\\d+}")
	public void deleteBeacon(@PathVariable int beaconId, Authentication auth) {
		
		AuthenticationToken authToken = ControllerUtil.getAuthToken(auth);
		LOGGER.info("[[ DELETE ]] deleteBeacon - beaconId: {}, userId: {}", beaconId, authToken.getId());
		
		BeaconLog log = new BeaconLog(
				beaconId,
				BeaconLogSeverity.INFO,
				BeaconLogType.SYSTEM,
				"Marked beacon as DELETED by user with id: " + authToken.getId() + ".",
				new Date()
				);
		
		try {
			this.beaconMapper.updateBeaconStatus(beaconId, BeaconStatus.DELETED);
			this.beaconMapper.insertBeaconLog(beaconId, log);
		} catch (Throwable t) {
			LOGGER.error("Error while deleting beacon: {}", t.getMessage(), t);
			throw new InternalException("Failed to delete beacon.", ClientError.BEACON_DELETE_FAILED);
		}
		
		LOGGER.info("[[ DELETE ]] deleteBeacon |END| - beaconId: {}, userId: {}", beaconId, authToken.getId());
		
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/users")
	public SearchResult<List<UserLight>> postFindUsers(@RequestBody Map<String, Object> filters,
									@RequestParam(required = false) Integer offset,
									@RequestParam(required = false) Integer limit,
									Authentication auth) {
		
		AuthenticationToken authToken = ControllerUtil.getAuthToken(auth);
		LOGGER.info("[[ POST ]] postFindUsers - by user with id: {}", authToken.getId());
		
		try {
			return this.adminService.getUsersLight(filters, limit, offset);
		} catch (BadRequestException e) {
			throw e;
		} catch (Throwable t) {
			LOGGER.error("Could not find users: {}", t.getMessage(), t);
			throw new InternalException("Failed to find users.", ClientError.GENERIC_ERROR);
		} finally {
			LOGGER.info("[[ POST ]] postFindUsers - by user with id: {} |END|", authToken.getId());
		}
		
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/users/bulk/{action}")
	public List<UserLight> postUserBulkAction(@PathVariable UserBulkAction action,
											  @RequestParam UUID tid,
											  @RequestBody UserBulkActionData data,
											  Authentication auth) {
		
		AuthenticationToken authToken = ControllerUtil.getAuthToken(auth);
		LOGGER.info("[[ POST ]] postUserBulkAction - action: {} by user with id: {}", action, authToken.getId());
		
		try {
			return this.adminService.executeBulkAction(action, tid, data);
		} finally {
			LOGGER.info("[[ POST ]] postUserBulkAction - action: {} by user with id: {} |END|", action, authToken.getId());
		}
		
	}
	
	@RequestMapping(method = RequestMethod.DELETE, path = "/users/{userId:\\d+}")
	public void deleteUser(@PathVariable int userId) {
		
		LOGGER.info("[[ DELETE ]] deleteUser - userId: " + userId);
		
		try {
			this.adminService.deleteUser(userId);
		} catch (Throwable t) {
			LOGGER.error("Could not delete user: {}", t.getMessage(), t);
			throw new InternalException("Failed to delete user.", ClientError.GENERIC_ERROR);
		}

		LOGGER.info("[[ DELETE ]] deleteUser |END| - userId: " + userId);
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/users/{userId:\\d+}/expireCredentials")
	public void getExpireCredentials(@PathVariable int userId) {
		
		LOGGER.info("[[ GET ]] getExpireCredentials - userId: " + userId);
		
		try {
			this.adminService.expireCredentials(userId);
		} catch (Throwable t) {
			LOGGER.error("Could not expire credentials: {}", t.getMessage(), t);
			throw new InternalException("Failed to expire credentials.", ClientError.GENERIC_ERROR);
		}

		LOGGER.info("[[ GET ]] getExpireCredentials |END| - userId: " + userId);
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/users/{userId:\\d+}/activate")
	public void getActivate(@PathVariable int userId) {
		
		LOGGER.info("[[ GET ]] getInactivate - userId: " + userId);
		
		try {
			this.adminService.activate(userId);
		} catch (Throwable t) {
			LOGGER.error("Could not activate user: {}", t.getMessage(), t);
			throw new InternalException("Failed to activate user.", ClientError.GENERIC_ERROR);
		}

		LOGGER.info("[[ GET ]] getActivate |END| - userId: " + userId);
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/users/{userId:\\d+}/inactivate")
	public void getInactivate(@PathVariable int userId) {
		
		LOGGER.info("[[ GET ]] getInactivate - userId: " + userId);
		
		try {
			this.adminService.inactivate(userId);
		} catch (Throwable t) {
			LOGGER.error("Could not inactivate user: {}", t.getMessage(), t);
			throw new InternalException("Failed to inactivate user.", ClientError.GENERIC_ERROR);
		}

		LOGGER.info("[[ GET ]] getInactivate |END| - userId: " + userId);
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/users/roles")
	public List<Role> getUserRoles() {
		
		LOGGER.info("[[ GET ]] getUserRoles");
		
		try {
			return this.adminService.getAllUserRoles();
		} catch (Throwable t) {
			LOGGER.error("Could not get user roles: {}", t.getMessage(), t);
			throw new InternalException("Failed to get user roles.", ClientError.GENERIC_ERROR);
		} finally {
			LOGGER.info("[[ GET ]] getUserRoles |END|");
		}
		
	}

	@RequestMapping(method = RequestMethod.GET, path = "/users/{userId:\\d+}/loginkey")
	public String getLoginKey(@PathVariable int userId) {
		
		LOGGER.info("[[ GET ]] getLoginKey - userId: {}", userId);
		
		try {
			return this.adminService.getLoginKey(userId);
		} catch (Throwable t) {
			LOGGER.error("Could not get secure login key for user: {}", t.getMessage(), t);
			throw new InternalException("Failed to get secure login key for user.", ClientError.FAILED_KEY_STORE);
		} finally {
			LOGGER.info("[[ GET ]] getLoginKey |END| - userId: {}", userId);
		}
		
	}
	
	@RequestMapping(method = RequestMethod.GET,
					path = "/users/{userId:\\d+}/loginkey/qr")
	public ResponseEntity<StreamingResponseBody> getPermanentLoginQr(@PathVariable int userId,
		 	 														 HttpServletResponse response) {
		
		LOGGER.debug("[[ GET ]] getPermanentLoginQr - generate permanent login QR for user: {}", userId);
		
		try {
			var user = this.adminService.findUser(userId);
			return ResponseEntity.ok()
					.header("Content-Disposition", "attachment; filename=\"login_qr_" + user.getUsername() + ".png\"")
					.cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
					.contentType(MediaType.IMAGE_PNG)
					.body(d -> this.adminService.writeLoginQr(user, response.getOutputStream()));
		} finally {
			LOGGER.debug("[[ GET ]] getPermanentLoginQr |END| Successfully generated login QR for user: {}", userId);
		}

	}
	
	@RequestMapping(method = RequestMethod.GET,
					path = "/users/loginkey/qr")
	public ResponseEntity<StreamingResponseBody> getBulkPermanentLoginQr(@RequestParam("tid") UUID transactionId,
																	 	 HttpServletResponse response) {
		
		LOGGER.debug("[[ GET ]] getBulkPermanentLoginQr - generate permanent login QR for users affected by bulk action {}", transactionId);

		List<Integer> userIds = this.appService.getTransaction(transactionId);
		List<UserLight> affectedUsers = this.adminService.findUsersLightById(userIds);
		
		try {
			return ResponseEntity.ok()
					.header("Content-Disposition", "attachment; filename=\"bulk_login_qr.zip\"")
					.cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(d -> this.adminService.writeBulkLoginQr(affectedUsers, response.getOutputStream()));
		} finally {
			LOGGER.debug("[[ GET ]] getBulkPermanentLoginQr |END| Successfully generated bulk permanent login QR for users");
		}

	}
	
	@RequestMapping(method = RequestMethod.PUT, path = "/users")
	public List<UserLight> putNewUsers(@Validated(ValidationGroups.Update.class) @RequestBody UserCreation creation, Authentication auth) {
				
		AuthenticationToken authToken = ControllerUtil.getAuthToken(auth);
		LOGGER.info("[[ PUT ]] putNewUser - called by: {}, users to create: {}", authToken.getId(), creation);
		
		try {
			return this.adminService.registerUsers(creation);
		} catch (DuplicateUsernameException e) {
			throw new BadRequestException("Username " + e.getUsername() + " already exists.", ClientError.USERNAME_DUPLICATE);
		} catch (Throwable t) {
			LOGGER.error("Could not create users: {}", t.getMessage(), t);
			throw new InternalException("Failed to create user.", ClientError.GENERIC_ERROR);
		} finally {
			LOGGER.info("[[ PUT ]] putNewUser |END| - called by: {}, users to create: {}", authToken.getId(), creation);
		}
		
	}

	@RequestMapping(method = RequestMethod.PUT, path = "/users/{userId:\\d+}/roles/add")
	public void putAddRole(@PathVariable int userId,
						   @Validated(ValidationGroups.Update.class) @RequestBody List<Role> roles,
						   Authentication auth) {
		
		LOGGER.info("[[ PUT ]] putAddRole - called by: {}, user to add roles to: {}, roles: {}", auth.getName(), userId, roles);
		
		try {
			this.adminService.modifyRoles(userId, roles, true);
		} catch (Throwable t) {
			LOGGER.error("Could not add roles: {}", t.getMessage(), t);
			throw new InternalException("Failed to add roles.", ClientError.GENERIC_ERROR);
		} finally {
			LOGGER.info("[[ PUT ]] putAddRole |END| - called by: {}, user to add roles to: {}, roles: {}", auth.getName(), userId, roles);
		}
		
	}

	@RequestMapping(method = RequestMethod.PUT, path = "/users/{userId:\\d+}/roles/remove")
	public void putRemoveRole(@PathVariable int userId,
						      @Validated(ValidationGroups.Update.class) @RequestBody List<Role> roles,
						      Authentication auth) {
		
		LOGGER.info("[[ PUT ]] putRemoveRole - called by: {}, user to remove roles from: {}, roles: {}", auth.getName(), userId, roles);
		
		try {
			this.adminService.modifyRoles(userId, roles, false);
		} catch (Throwable t) {
			LOGGER.error("Could not remove roles: {}", t.getMessage(), t);
			throw new InternalException("Failed to remove roles.", ClientError.GENERIC_ERROR);
		} finally {
			LOGGER.info("[[ PUT ]] putRemoveRole |END| - called by: {}, user to remove roles from: {}, roles: {}", auth.getName(), userId, roles);
		}
		
	}

	@RequestMapping(method = RequestMethod.GET, path = "/phenology/types")
	public List<PhenologyObservationType> getAllPhenologyObservationTypes() {
		
		LOGGER.info("[[ GET ]] getAllPhenologyObservationTypes");
		
		try {
			return this.phenologyMapper.getAllObservationTypes();
		} catch (Throwable t) {
			LOGGER.error("Could not get all phenology observation types: {}", t.getMessage(), t);
			throw new InternalException("Failed to get all phenology observation types.", ClientError.GENERIC_ERROR);
		} finally {
			LOGGER.info("[[ GET ]] getAllPhenologyObservationTypes |END|");
		}
		
	}

	@RequestMapping(method = RequestMethod.GET, path = "/announcements")
	public List<Announcement> getAllAnnouncements() {
		
		LOGGER.info("[[ GET ]] getAllAnnouncements");
		
		try {
			return this.uiMapper.getAllAnnouncements();
		} catch (Throwable t) {
			LOGGER.error("Could not get all announcements: {}", t.getMessage(), t);
			throw new InternalException("Failed to get all announcements.", ClientError.GENERIC_ERROR);
		} finally {
			LOGGER.info("[[ GET ]] getAllAnnouncements |END|");
		}
		
	}

	@RequestMapping(method = RequestMethod.PUT, path = "/announcement")
	public void putAnnouncement(@Validated(ValidationGroups.Update.class) @RequestBody Announcement announcement,
								Authentication auth) {
		
		LOGGER.info("[[ PUT ]] putAnnouncement");
		
		final int userId = ControllerUtil.getAuthToken(auth).getId();
		try {
			this.uiMapper.insertAnnouncement(announcement, String.valueOf(userId));
		} catch (Throwable t) {
			LOGGER.error("Could not insert announcement: {}", t.getMessage(), t);
			throw new InternalException("Failed to insert announcement.", ClientError.GENERIC_ERROR);
		} finally {
			LOGGER.info("[[ PUT ]] putAnnouncement |END|");
		}
		
	}

	@RequestMapping(method = RequestMethod.DELETE, path = "/announcement/{announcementId:\\d+}")
	public void deleteAnnouncement(@PathVariable("announcementId") int announcementId) {
		
		LOGGER.info("[[ PUT ]] putAnnouncement");
		
		try {
			this.uiMapper.deleteAnnouncement(announcementId);
		} catch (Throwable t) {
			LOGGER.error("Could not insert announcement: {}", t.getMessage(), t);
			throw new InternalException("Failed to insert announcement.", ClientError.GENERIC_ERROR);
		} finally {
			LOGGER.info("[[ PUT ]] putAnnouncement |END|");
		}
		
	}

	@RequestMapping(method = RequestMethod.GET, path = "/beacon/logs")
	public List<BeaconLog> getBeaconLogs(@RequestParam(required = false) Long beaconId,
										 @RequestParam(required = false) BeaconLogSeverity minSeverity,
										 @RequestParam(required = false) Integer offset,
										 @RequestParam(required = false) Integer maxLogs,
										 @RequestParam(required = false) String timespanMin,
										 @RequestParam(required = false) String timespanMax) {
		
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("[[ GET ]] getBeaconLogs - beaconId: " + beaconId +
					", minSeverity: " + minSeverity +
					", offset: " + offset +
					", maxLogs: " + maxLogs +
					", timespanMin: " + timespanMin +
					", timespanMax: " + timespanMax);			
		}
		
		try {
			
			List<BeaconLogSeverity> severities = null;
			if (minSeverity != null) {
				severities = new ArrayList<>();
				switch(minSeverity) {
					case TRACE:
						severities.add(BeaconLogSeverity.TRACE);
					case DEBUG:
						severities.add(BeaconLogSeverity.DEBUG);
					case INFO:
						severities.add(BeaconLogSeverity.INFO);
					case WARN:
						severities.add(BeaconLogSeverity.WARN);
					case ERROR:
						severities.add(BeaconLogSeverity.ERROR);
						break;
					default:
						LOGGER.error("User requetsed illegal beacon log severity: " + minSeverity);
						throw new BadRequestException("Invalid beacon log serverity given", ClientError.BEACON_LOG_SEVERITY_INVALID);
				}
			}
			
			if (beaconId == null) {
				beaconId = -1l;
			}
			if (offset == null) {
				offset = -1;
			}
			if (maxLogs == null) {
				maxLogs = -1;
			}
			
			Timespan timespan = ControllerUtil.getTimespanParams(this.dateFormatPattern, timespanMin, timespanMax);
			
			return this.beaconMapper.findBeaconLogs(
					beaconId,
					severities,
					offset,
					maxLogs,
					timespan.getStart(),
					timespan.getEnd()
			);
		} catch (BadRequestException e) {
			throw e;
		} catch (Throwable t) {
			LOGGER.error("Could not get beacon logs for beacon with id {}: {}", beaconId, t.getMessage(), t);
			throw new InternalException("Could not get beacon logs for beacon: " + t.getMessage(), ClientError.GENERIC_ERROR);
		} finally {
			LOGGER.trace("[[ GET ]] getBeaconLogs |END|");
		}
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/report")
	public List<Report> getReports(
			@PathVariable(required = false) Boolean autoCreate,
			@RequestParam(required = false) Boolean resolved,
			@RequestParam(required = false) Integer maxReports,
			@RequestParam(required = false) Integer offset,
			@RequestParam(required = false) String timespanMin,
			@RequestParam(required = false) String timespanMax) {
		
		LOGGER.debug("[[ GET ]] getReports");
		
		if (maxReports == null) {
			maxReports = -1; // infinite
		}
		
		Timespan timespan = ControllerUtil.getTimespanParams(this.dateFormatPattern, timespanMin, timespanMax);

		List<Report> reports = 
				this.appMapper.findReports(
						null,
						autoCreate,
						resolved,
						offset,
						maxReports,
						timespan.getStart(),
						timespan.getEnd());
		return reports;
		
	}
	
	@RequestMapping(method = RequestMethod.PUT, path = "/report/{id:\\d+}/remark")
	public void putReportRemark(
			@PathVariable int id,
			@RequestBody(required = false) String remark) {
		LOGGER.debug("[[ PUT ]] putReportRemark - id: " + id);
		this.appMapper.updateReportRemark(id, remark);
	}
	
	@RequestMapping(method = RequestMethod.PUT, path = "/report/{id:\\d+}/resolve")
	public void putReportResolve(@PathVariable int id) {
		LOGGER.debug("[[ PUT ]] putReportResolve - id: " + id);
		this.appMapper.updateReportResolved(id, true);
	}
	
	@RequestMapping(method = RequestMethod.PUT, path = "/report/{id:\\d+}/unresolve")
	public void putReportUnresolve(@PathVariable int id) {
		LOGGER.debug("[[ PUT ]] putReportUnresolve - id: " + id);
		this.appMapper.updateReportResolved(id, false);
	}
	
}
