package at.sparklingscience.urbantrees.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
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
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.NotFoundException;
import at.sparklingscience.urbantrees.mapper.BeaconMapper;

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
	
	@RequestMapping(method = RequestMethod.GET, path = "/{beaconId:\\d+}")
	public Beacon getBeacon(@PathVariable int beaconId) {
		
		Beacon beacon = this.beaconMapper.findBeaconById(beaconId);
		if (beacon == null) {
			throw new NotFoundException("No beacon found for given id.");
		}
		
		return beacon;
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/{address:(?:[\\d\\w]{2}\\-){5}(?:[\\d\\w]{2})}")
	public Beacon getBeaconByAddress(@PathVariable String address) {
		
		Beacon beacon = this.beaconMapper.findBeaconByAddress(address);
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
	@RequestMapping(method = RequestMethod.POST, path = "/{beaconId:\\d+}/data")
	public BeaconDataset postBeaconData(@PathVariable int beaconId, @RequestBody BeaconDataset dataset) {
		
		if (beaconId != dataset.getBeaconId()) {
			throw new BadRequestException("Datasets' beacon id does not match the paths' beacon id.");
		}
		
		this.beaconMapper.insertBeaconDataset(dataset);
		
		return dataset;
		
	}

}
