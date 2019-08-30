package at.sparklingscience.urbantrees.controller;

import java.security.Principal;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import at.sparklingscience.urbantrees.controller.util.ControllerUtil;
import at.sparklingscience.urbantrees.controller.util.Timespan;
import at.sparklingscience.urbantrees.domain.Beacon;
import at.sparklingscience.urbantrees.domain.BeaconDataset;
import at.sparklingscience.urbantrees.domain.BeaconLog;
import at.sparklingscience.urbantrees.domain.BeaconReadoutResult;
import at.sparklingscience.urbantrees.domain.BeaconSettings;
import at.sparklingscience.urbantrees.domain.BeaconStatus;
import at.sparklingscience.urbantrees.domain.UserLevelAction;
import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.NotFoundException;
import at.sparklingscience.urbantrees.mapper.BeaconMapper;
import at.sparklingscience.urbantrees.service.UserService;

/**
 * 
 * TODO this whole class needs api doc
 *
 */
@RestController
@RequestMapping("/beacon")
public class BeaconController {
	
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BeaconController.class);
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private BeaconMapper beaconMapper;
	
	@Value("${at.sparklingscience.urbantrees.dateFormatPattern}")
	private String dateFormatPattern;
	
	@RequestMapping(method = RequestMethod.GET)
	public List<Beacon> getAllBeaconsActive() {
		
		LOGGER.debug("[[ GET ]] getAllBeaconsActive");
		
		List<Beacon> beacons = this.beaconMapper.findAllBeaconsActive();
		if (beacons == null) {
			throw new NotFoundException("No beacon found.");
		}
		
		LOGGER.debug("[[ GET ]] getAllBeaconsActive |END|");
		
		return beacons;
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/status/{beaconStatus}")
	public List<Beacon> getAllBeaconsByStatus(@PathVariable String beaconStatus) {
		
		final BeaconStatus status = BeaconStatus.valueOf(beaconStatus);
		
		LOGGER.debug("[[ GET ]] getAllBeaconsByStatus - status: {}", status);
		
		List<Beacon> beacons = this.beaconMapper.findAllBeaconsByStatus(status);		
		if (beacons == null) {
			throw new NotFoundException("No beacon with status " + beaconStatus + " found.");
		}
		
		return beacons;
		
	}
	
	@RequestMapping(method = RequestMethod.PUT, path = "/{beaconId:\\d+}/status/{beaconStatus}")
	public void putBeaconStatus(@PathVariable int beaconId, @PathVariable String beaconStatus) {
		
		final BeaconStatus status = BeaconStatus.valueOf(beaconStatus);
		
		LOGGER.debug("[[ PUT ]] putBeaconStatus - beaconId: {}, status: {}", beaconId, status);
		
		try {
			this.beaconMapper.updateBeaconStatus(beaconId, status);					
		} catch (Throwable t) {
			throw new InternalError("Update of beacon " + beaconId + " to status " + status + " failed.");			
		}
		
		LOGGER.info("[[ POST ]] putBeaconStatus |END| - beaconId: {}, status: {}", beaconId, status);
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/{beaconId:\\d+}")
	public Beacon getBeacon(@PathVariable int beaconId,
							Authentication auth) {
		
		LOGGER.debug("[[ GET ]] getBeacon - beaconId: {}", beaconId);
		
		Beacon beacon = this.beaconMapper.findBeaconById(beaconId);
		if (beacon == null) {
			throw new NotFoundException("No beacon found for given id.");
		}
		
		return beacon;
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/{beaconAddress:(?:[\\d\\w]{2}\\:){5}(?:[\\d\\w]{2})}")
	public Beacon getBeaconByAddress(@PathVariable String beaconAddress) {
		
		LOGGER.debug("[[ GET ]] getBeaconByAddress - beaconAddress: {}", beaconAddress);
		
		Beacon beacon = this.beaconMapper.findBeaconByAddress(beaconAddress);
		if (beacon == null) {
			throw new NotFoundException("No beacon found for given address.");
		}
		
		return beacon;
		
	}

	@RequestMapping(method = RequestMethod.GET, path = "/{beaconId:\\d+}/data")
	public List<BeaconDataset> getBeaconData(
			@PathVariable int beaconId,
			@RequestParam(required = false) Integer maxDatapoints,
			@RequestParam(required = false) String timespanMin,
			@RequestParam(required = false) String timespanMax) {
		
		LOGGER.debug("[[ GET ]] getBeaconData - beaconId: {}", beaconId);
		
		if (maxDatapoints == null) {
			maxDatapoints = -1; // infinite
		}
		
		Timespan timespan = ControllerUtil.getTimespanParams(this.dateFormatPattern, timespanMin, timespanMax);

		List<BeaconDataset> datasets = 
				this.beaconMapper.findBeaconDataByBeaconId(
						beaconId,
						maxDatapoints,
						timespan.getStart(),
						timespan.getEnd()
						);

		return datasets;
		
	}
	
	@Transactional
	@RequestMapping(method = RequestMethod.PUT, path = "/{beaconId:\\d+}/readout")
	public void putBeaconReadoutResult(
			@PathVariable int beaconId,
			@Validated(ValidationGroups.Update.class) @RequestBody BeaconReadoutResult result,
			Authentication auth) {
		
		LOGGER.info("[[ PUT ]] putBeaconReadoutResult - beaconId: {}", beaconId);
		
		if (result == null || result.getDatasets() == null || result.getSettings() == null) {
			throw new BadRequestException("Beacon data/settings is null, can't continue with putBeaconData for beaconId: " + beaconId);
		}
		
		List<BeaconDataset> datasets = result.getDatasets();
		int loggingIntSec = result.getSettings().getLoggingIntervalSec();
		long readoutTimeApprox = System.currentTimeMillis() - result.getTimeSinceDataReadoutMs();
		long logTime = result.getSettings().getRefTime().getTime();
		while (logTime <= readoutTimeApprox - (long) loggingIntSec * 1000l) {
			logTime += (long) loggingIntSec * 1000l;
		}
		for (int i = datasets.size()-1; i >= 0; i--) {
			datasets.get(i).setObservationDate(new Date(logTime));
			logTime -= (long) loggingIntSec * 1000l;
		}
		
		this.beaconMapper.insertBeaconDatasets(beaconId, datasets);
		this.beaconMapper.insertBeaconSettings(beaconId, result.getSettings(), null);

		this.beaconMapper.updateBeaconStatus(beaconId, BeaconStatus.OK);
		this.userService.increaseXp(UserLevelAction.BEACON_READOUT, auth);
		
		LOGGER.info("[[ PUT ]] postBeaconputBeaconReadoutResultData |END| - beaconId: {}, inserted {} datasets", beaconId, datasets.size());
		
	}
	
	@Transactional
	@RequestMapping(method = RequestMethod.PUT, path = "/{beaconId:\\d+}/data")
	public void putBeaconData(
			@PathVariable int beaconId,
			@Validated(ValidationGroups.Update.class) @RequestBody List<BeaconDataset> datasets,
			Authentication auth) {
		
		LOGGER.info("[[ PUT ]] putBeaconData - beaconId: {}", beaconId);
		
		if (datasets == null) {
			throw new BadRequestException("Beacon datasets are null, can't continue with postBeaconData for beaconId: " + beaconId);
		}
		
		this.beaconMapper.insertBeaconDatasets(beaconId, datasets);
		
		LOGGER.info("[[ PUT ]] putBeaconData |END| - beaconId: {}, inserted {} datasets", beaconId, datasets.size());
		
		this.userService.increaseXp(UserLevelAction.BEACON_READOUT, auth);
		
	}
	
	@Transactional
	@RequestMapping(method = RequestMethod.GET, path = "/{beaconId:\\d+}/settings")
	public BeaconSettings getLatestBeaconSettings(@PathVariable int beaconId) {
		
		LOGGER.info("[[ GET ]] getLatestBeaconSettings - beaconId: {}", beaconId);
		
		final BeaconSettings latestSettings = this.beaconMapper.findLatestBeaconSettingsByBeaconId(beaconId);
		if (latestSettings == null) {
			throw new NotFoundException("Could not find beacon settings for beacon " + beaconId);
		}
		
		LOGGER.info("[[ GET ]] getLatestBeaconSettings |END| - beaconId: {}, settings id: {}", beaconId, latestSettings.getId());
		
		return latestSettings;
		
	}
	
	@Transactional
	@RequestMapping(method = RequestMethod.PUT, path = "/{beaconId:\\d+}/settings")
	public void putBeaconSettings(
			@PathVariable int beaconId,
			@Validated(ValidationGroups.Update.class) @RequestBody BeaconSettings settings,
			Principal principal) {
		
		LOGGER.info("[[ PUT ]] putBeaconSettings - beaconId: {}", beaconId);
		
		if (settings == null) {
			throw new BadRequestException("Beacon settings are null, can't continue with putBeaconSettings for beaconId: " + beaconId);
		}
		
		this.beaconMapper.insertBeaconSettings(beaconId, settings, null);
		this.beaconMapper.updateBeaconStatus(beaconId, BeaconStatus.OK);
		
		LOGGER.info("[[ PUT ]] putBeaconSettings |END| - beaconId: {}, inserted settings", beaconId);
		
	}
	
	@Transactional
	@RequestMapping(method = RequestMethod.PUT, path = "/logs")
	public void putBeaconLogs(@Validated(ValidationGroups.Update.class)
							  @RequestBody List<BeaconLog> logs) {
		
		LOGGER.info("[[ PUT ]] putBeaconLogs - inserting {} logs", logs.size());
		
		this.beaconMapper.insertBeaconLogs(logs);
		
		LOGGER.info("[[ PUT ]] putBeaconLogs |END| - successfully inserted {} logs", logs.size());
		
	}

}
