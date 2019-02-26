package at.sparklingscience.urbantrees.controller;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import at.sparklingscience.urbantrees.domain.Beacon;
import at.sparklingscience.urbantrees.domain.BeaconLog;
import at.sparklingscience.urbantrees.domain.BeaconLogSeverity;
import at.sparklingscience.urbantrees.domain.BeaconLogType;
import at.sparklingscience.urbantrees.domain.BeaconStatus;
import at.sparklingscience.urbantrees.domain.City;
import at.sparklingscience.urbantrees.domain.PhenologyObservationType;
import at.sparklingscience.urbantrees.domain.Role;
import at.sparklingscience.urbantrees.domain.Tree;
import at.sparklingscience.urbantrees.domain.User;
import at.sparklingscience.urbantrees.domain.UserLight;
import at.sparklingscience.urbantrees.domain.ui.Announcement;
import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.ClientError;
import at.sparklingscience.urbantrees.exception.InternalException;
import at.sparklingscience.urbantrees.mapper.AuthMapper;
import at.sparklingscience.urbantrees.mapper.BeaconMapper;
import at.sparklingscience.urbantrees.mapper.PhenologyMapper;
import at.sparklingscience.urbantrees.mapper.TreeMapper;
import at.sparklingscience.urbantrees.mapper.UiMapper;
import at.sparklingscience.urbantrees.security.user.AuthenticationService;

@RestController
@RequestMapping("/admin")
public class AdminController {
	
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
	
	/**
	 * String to prepend all mod_user & cre_user inserts with.
	 */
	private static final String PREPEND_USER_INSERTS = "WEBADM_";
	
	@Autowired
	private TreeMapper treeMapper;
	
	@Autowired
	private BeaconMapper beaconMapper;
	
	@Autowired
	private PhenologyMapper phenologyMapper;

	@Autowired
	private AuthMapper authMapper;
	
	@Autowired
	private UiMapper uiMapper;
	
	@Autowired
	private AuthenticationService authService;
	
	@Transactional
	@RequestMapping(method = RequestMethod.POST, path = "/city")
	public City postCity(@Validated(ValidationGroups.Update.class) @RequestBody City city, Authentication auth) {
		
		LOGGER.info("[[ POST ]] postCity - city: {}", city.getName());
		
		try {
			this.treeMapper.insertCity(city, PREPEND_USER_INSERTS + auth.getName());
		} catch (DuplicateKeyException ex) {
			LOGGER.debug("Admin tried to enter duplicate city: {}", ex.getMessage(), ex);
			throw new BadRequestException("There is already a city with given name.", ClientError.CITY_DUPLICATE);
		} catch (Throwable t) {
			LOGGER.debug("Internal excetion during postCity: {}", t.getMessage(), t);
			throw new InternalException("Internal error encountered while adding city.", ClientError.CITY_INTERNAL_ERROR);
		}
		
		LOGGER.info("[[ POST ]] postCity |END| - city: {}, inserted city id: {}", city.getName(), city.getId());
		
		return city;
		
	}
	
	@Transactional
	@RequestMapping(method = RequestMethod.POST, path = "/beacon")
	public Beacon postBeacon(@Validated(ValidationGroups.Update.class) @RequestBody Beacon beacon, Authentication auth) {
		
		LOGGER.info("[[ POST ]] postBeacon - deviceId: {}", beacon.getDeviceId());
		
		// initialize default values
		beacon.setStatus(BeaconStatus.INITIAL);
		
		final String name = auth.getName();
		try {
			this.beaconMapper.insertBeacon(beacon, PREPEND_USER_INSERTS + name);
			this.beaconMapper.insertBeaconSettings(beacon.getId(), beacon.getSettings(), PREPEND_USER_INSERTS + name);
		} catch (DuplicateKeyException ex) {
			LOGGER.debug("Admin tried to enter duplicate beacon: {}", ex.getMessage(), ex);
			throw new BadRequestException("There is already a beacon with given deviceId or same address.", ClientError.BEACON_DUPLICATE);
		} catch (Throwable t) {
			LOGGER.debug("Internal excetion during postBeacon: {}", t.getMessage(), t);
			throw new InternalException("Internal error encountered while adding beacon.", ClientError.BEACON_INTERNAL_ERROR);
		}
		
		LOGGER.info("[[ POST ]] postBeacon |END| - deviceId: {}, inserted beacon id: {}", beacon.getDeviceId(), beacon.getId());
		
		return beacon;
		
	}

	@Transactional
	@RequestMapping(method = RequestMethod.POST, path = "/tree")
	public Tree postTree(@Validated(ValidationGroups.Update.class) @RequestBody Tree tree, Authentication auth) {
		
		LOGGER.info("[[ POST ]] postTree");
		
		final String name = auth.getName();
		try {
			this.treeMapper.insertTreeLocation(tree.getLocation(), name);
			this.treeMapper.insertTree(tree, name);
			this.treeMapper.insertTreeAge(tree, name);
		} catch (Throwable t) {
			LOGGER.debug("Internal excetion during postTree: {}", t.getMessage(), t);
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
		
		LOGGER.info("[[ POST ]] postTreeUpdate - tree id: {}", tree.getId());
		
		try {
			this.treeMapper.updateTree(tree, auth.getName());
		} catch (Throwable t) {
			LOGGER.debug("Internal excetion during postTreeUpdate: {}", t.getMessage(), t);
			throw new BadRequestException("Internal error encountered while updating tree.", ClientError.TREE_UPDATE_FAILED);
		}
		
		LOGGER.info("[[ POST ]] postTreeUpdate |END| - tree id: {}", tree.getId());
		
		return tree;
		
	}

	@Transactional
	@RequestMapping(method = RequestMethod.DELETE, path = "/beacon/{beaconId:\\d+}")
	public void deleteBeacon(@PathVariable int beaconId, Authentication auth) {
		
		LOGGER.info("[[ DELETE ]] deleteBeacon - beaconId: {}", beaconId);
		
		BeaconLog log = new BeaconLog(
				beaconId,
				BeaconLogSeverity.INFO,
				BeaconLogType.SYSTEM,
				"Marked beacon as DELETED by user: " + auth.getName() + ".",
				new Date()
				);
		
		try {
			this.beaconMapper.updateBeaconStatus(beaconId, BeaconStatus.DELETED);
			this.beaconMapper.insertBeaconLog(beaconId, log);
		} catch (Throwable t) {
			LOGGER.debug("Error while deleting beacon: {}", t.getMessage(), t);
			throw new InternalException("Failed to delete beacon.", ClientError.BEACON_DELETE_FAILED);
		}
		
		LOGGER.info("[[ DELETE ]] deleteBeacon |END| - beaconId: {}", beaconId);
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/users")
	public List<UserLight> getUsers() {
		
		LOGGER.info("[[ GET ]] getUsers");
		
		try {
			List<UserLight> users = this.authMapper.findAllUsersLight();
			for (UserLight user : users) {
				user.setNonLocked(this.authService.isUserNonLocked(user));
			}
			return users;
		} catch (Throwable t) {
			LOGGER.debug("Could not find users: {}", t.getMessage(), t);
			throw new InternalException("Failed to find users.", ClientError.GENERIC_ERROR);
		} finally {
			LOGGER.info("[[ GET ]] getUsers |END|");
		}
		
	}
	
	@RequestMapping(method = RequestMethod.DELETE, path = "/users/{userId:\\d+}")
	public void deleteUser(@PathVariable int userId) {
		
		LOGGER.info("[[ DELETE ]] deleteUser - userId: " + userId);
		
		try {
			this.authMapper.deleteUser(userId);
		} catch (Throwable t) {
			LOGGER.debug("Could not delete user: {}", t.getMessage(), t);
			throw new InternalException("Failed to delete user.", ClientError.GENERIC_ERROR);
		}

		LOGGER.info("[[ DELETE ]] deleteUser |END| - userId: " + userId);
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/users/{userId:\\d+}/expireCredentials")
	public void getExpireCredentials(@PathVariable int userId) {
		
		LOGGER.info("[[ GET ]] getExpireCredentials - userId: " + userId);
		
		try {
			this.authService.expireCredentials(userId);
		} catch (Throwable t) {
			LOGGER.debug("Could not expire credentials: {}", t.getMessage(), t);
			throw new InternalException("Failed to expire credentials.", ClientError.GENERIC_ERROR);
		}

		LOGGER.info("[[ GET ]] getExpireCredentials |END| - userId: " + userId);
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/users/{userId:\\d+}/activate")
	public void getActivate(@PathVariable int userId) {
		
		LOGGER.info("[[ GET ]] getInactivate - userId: " + userId);
		
		try {
			this.authService.activate(userId);
		} catch (Throwable t) {
			LOGGER.debug("Could not activate user: {}", t.getMessage(), t);
			throw new InternalException("Failed to activate user.", ClientError.GENERIC_ERROR);
		}

		LOGGER.info("[[ GET ]] getActivate |END| - userId: " + userId);
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/users/{userId:\\d+}/inactivate")
	public void getInactivate(@PathVariable int userId) {
		
		LOGGER.info("[[ GET ]] getInactivate - userId: " + userId);
		
		try {
			this.authService.inactivate(userId);
		} catch (Throwable t) {
			LOGGER.debug("Could not inactivate user: {}", t.getMessage(), t);
			throw new InternalException("Failed to inactivate user.", ClientError.GENERIC_ERROR);
		}

		LOGGER.info("[[ GET ]] getInactivate |END| - userId: " + userId);
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/users/roles")
	public List<Role> getUserRoles() {
		
		LOGGER.info("[[ GET ]] getUserRoles");
		
		try {
			return this.authMapper.findAllUserRoles();
		} catch (Throwable t) {
			LOGGER.debug("Could not inactivate user: {}", t.getMessage(), t);
			throw new InternalException("Failed to inactivate user.", ClientError.GENERIC_ERROR);
		} finally {
			LOGGER.info("[[ GET ]] getUserRoles |END|");
		}
		
	}

	@RequestMapping(method = RequestMethod.GET, path = "/users/{userId:\\d+}/loginkey")
	public String getLoginKey(@PathVariable int userId) {
		
		LOGGER.info("[[ GET ]] getLoginKey - userId: {}", userId);
		
		try {
			return this.authService.getLoginKey(userId);
		} catch (Throwable t) {
			LOGGER.debug("Could not get secure login key for user: {}", t.getMessage(), t);
			throw new InternalException("Failed to get secure login key for user.", ClientError.FAILED_KEY_STORE);
		} finally {
			LOGGER.info("[[ GET ]] getLoginKey |END| - userId: {}", userId);
		}
		
	}
	
	@RequestMapping(method = RequestMethod.PUT, path = "/user")
	public void putNewUser(@Validated(ValidationGroups.Update.class) @RequestBody User user, Authentication auth) {
		
		LOGGER.info("[[ PUT ]] putNewUser - called by: {}, user to create: {}", auth.getName(), user);
		
		try {
			this.authService.registerUser(user.getUsername(), null, user.getRoles());
		} catch (Throwable t) {
			LOGGER.debug("Could not create user: {}", t.getMessage(), t);
			throw new InternalException("Failed to create user.", ClientError.GENERIC_ERROR);
		} finally {
			LOGGER.info("[[ PUT ]] putNewUser |END| - called by: {}, user to create: {}", auth.getName(), user);
		}
		
	}

	@RequestMapping(method = RequestMethod.PUT, path = "/users/{userId:\\d+}/roles/add")
	public void putAddRole(@PathVariable int userId,
						   @Validated(ValidationGroups.Update.class) @RequestBody List<Role> roles,
						   Authentication auth) {
		
		LOGGER.info("[[ PUT ]] putAddRole - called by: {}, user to add roles to: {}, roles: {}", auth.getName(), userId, roles);
		
		try {
			this.authService.modifyRoles(userId, roles, true);
		} catch (Throwable t) {
			LOGGER.debug("Could not add roles: {}", t.getMessage(), t);
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
			this.authService.modifyRoles(userId, roles, false);
		} catch (Throwable t) {
			LOGGER.debug("Could not remove roles: {}", t.getMessage(), t);
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
			LOGGER.debug("Could not get all phenology observation types: {}", t.getMessage(), t);
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
			LOGGER.debug("Could not get all announcements: {}", t.getMessage(), t);
			throw new InternalException("Failed to get all announcements.", ClientError.GENERIC_ERROR);
		} finally {
			LOGGER.info("[[ GET ]] getAllAnnouncements |END|");
		}
		
	}

	@RequestMapping(method = RequestMethod.PUT, path = "/announcement")
	public void putAnnouncement(@Validated(ValidationGroups.Update.class) @RequestBody Announcement announcement,
								Authentication auth) {
		
		LOGGER.info("[[ PUT ]] putAnnouncement");
		
		final String name = auth.getName();
		try {
			this.uiMapper.insertAnnouncement(announcement, PREPEND_USER_INSERTS + name);
		} catch (Throwable t) {
			LOGGER.debug("Could not insert announcement: {}", t.getMessage(), t);
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
			LOGGER.debug("Could not insert announcement: {}", t.getMessage(), t);
			throw new InternalException("Failed to insert announcement.", ClientError.GENERIC_ERROR);
		} finally {
			LOGGER.info("[[ PUT ]] putAnnouncement |END|");
		}
		
	}
	
}
