package at.sparklingscience.urbantrees.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import at.sparklingscience.urbantrees.domain.BeaconSettings;
import at.sparklingscience.urbantrees.domain.BeaconStatus;
import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.NotFoundException;
import at.sparklingscience.urbantrees.mapper.BeaconMapper;

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
	private BeaconMapper beaconMapper;
	
	@Value("${at.sparklingscience.urbantrees.dateFormatPattern}")
	private String dateFormatPattern;
	
	@RequestMapping(method = RequestMethod.GET, path = "/all/status/{beaconStatus}")
	public List<Beacon> getAllBeaconsByStatus(@PathVariable String beaconStatus) {
		
		LOGGER.debug("[[ GET ]] getAllBeaconsByStatus - status: {}", beaconStatus);
		
		List<Beacon> beacons = this.beaconMapper.findAllBeaconsByStatus(BeaconStatus.valueOf(beaconStatus));
		if (beacons == null) {
			throw new NotFoundException("No beacon with status " + beaconStatus + " found.");
		}
		
		return beacons;
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/{beaconId:\\d+}")
	public Beacon getBeacon(@PathVariable int beaconId) {
		
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
			@RequestParam(required = false) String timespanMin,
			@RequestParam(required = false) String timespanMax) {
		
		LOGGER.debug("[[ GET ]] getBeaconData - beaconId: {}", beaconId);
		
		Timespan timespan = ControllerUtil.getTimespanParams(this.dateFormatPattern, timespanMin, timespanMax);

		List<BeaconDataset> datasets = 
				this.beaconMapper.findBeaconDataByBeaconId(
						beaconId,
						timespan.getStart(),
						timespan.getEnd()
						);

		return datasets;
		
	}
	
	@Transactional
	@RequestMapping(method = RequestMethod.PUT, path = "/{beaconId:\\d+}/data")
	public void putBeaconData(
			@PathVariable int beaconId,
			@Validated(ValidationGroups.Update.class) @RequestBody List<BeaconDataset> datasets) {
		
		LOGGER.info("[[ POST ]] postBeaconData - beaconId: {}", beaconId);
		
		if (datasets == null) {
			throw new BadRequestException("Beacon datasets are null, can't continue with postBeaconData for beaconId: " + beaconId);
		}
		
		this.beaconMapper.insertBeaconDatasets(beaconId, datasets);
		
		LOGGER.info("[[ POST ]] postBeaconData |END| - beaconId: {}, inserted {} datasets", beaconId, datasets.size());
		
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
			@Validated(ValidationGroups.Update.class) @RequestBody BeaconSettings settings) {
		
		LOGGER.info("[[ PUT ]] putBeaconSettings - beaconId: {}", beaconId);
		
		if (settings == null) {
			throw new BadRequestException("Beacon settings are null, can't continue with putBeaconSettings for beaconId: " + beaconId);
		}
		
		this.beaconMapper.insertBeaconSettings(beaconId, settings);
		
		LOGGER.info("[[ PUT ]] putBeaconSettings |END| - beaconId: {}, inserted settings", beaconId);
		
	}
	
	@Transactional
	@RequestMapping(method = RequestMethod.PUT, path = "/{beaconId:\\d+}/logs")
	public void putBeaconLogs(
			@PathVariable int beaconId,
			@Validated(ValidationGroups.Update.class) @RequestBody List<BeaconLog> logs) {
		
		LOGGER.info("[[ PUT ]] putBeaconLogs - beaconId: {}", beaconId);
		
		if (logs == null) {
			throw new BadRequestException("Beacon logs are null, can't continue with putBeaconSettings for beaconId: " + beaconId);
		}
		
		this.beaconMapper.insertBeaconLogs(beaconId, logs);
		
		LOGGER.info("[[ PUT ]] putBeaconLogs |END| - beaconId: {}, inserted settings", beaconId);
		
	}

}
