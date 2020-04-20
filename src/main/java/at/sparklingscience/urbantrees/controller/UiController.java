package at.sparklingscience.urbantrees.controller;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import at.sparklingscience.urbantrees.domain.ui.Announcement;
import at.sparklingscience.urbantrees.domain.ui.Image;
import at.sparklingscience.urbantrees.domain.ui.MeasurementStatistics;
import at.sparklingscience.urbantrees.domain.ui.SystemStatistics;
import at.sparklingscience.urbantrees.mapper.UiMapper;

/**
 * Controller for ui specific REST-calls.
 * 
 * @author Laurenz Fiala
 * @since 2018/02/04
 */
@RestController
@RequestMapping("/ui")
public class UiController {
	
	private static Logger logger;
	
	@Autowired
    private UiMapper uiMapper;
	
	@Value("${at.sparklingscience.urbantrees.dateFormatPattern}")
	private String dateFormatPattern;
	
	public UiController(Logger classLogger) {
		logger = classLogger;
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/phenology/observation/result/{treeSpeciesId:\\d+}/{resultId:\\d+}/img")
	public Image getPhenologyObservationResultImage(
			@PathVariable int treeSpeciesId,
			@PathVariable int resultId) {
		
		logger.debug("[[ GET ]] getPhenologyObservationResultImage - treeSpeciesId: {}, resultId: {}", treeSpeciesId, resultId);
		
		Image img = this.uiMapper.findImageForPhenologyObservationResult(treeSpeciesId, resultId);
		
		if (img == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		} else {
			return img;
		}
		
	}
	
	/*
	 * TODO API def
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/announcements")
	public List<Announcement> getCurrentAnnouncements() {
		
		logger.trace("[[ GET ]] getCurrentAnnouncements");
		
		return this.uiMapper.getCurrentAnnouncements();
		
	}
	
	/*
	 * TODO API def
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/statistics/system")
	public SystemStatistics getSystemStatistics() {
		
		logger.trace("[[ GET ]] getSystemStatistics");
		
	    SystemStatistics stats = this.uiMapper.getSystemStatistics();
	    stats.setBeaconReadouts(this.uiMapper.getBeaconReadoutDaily());
	    stats.setPhenologyObservations(this.uiMapper.getPhenologyObservationDaily());
	
	    return stats;
	    
	}
	
	/*
	 * TODO API def
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/statistics/measurements")
	public MeasurementStatistics getMeasurementStatistics() {
		
		logger.trace("[[ GET ]] getMeasurementStatistics");
		
	    return this.uiMapper.getMeasurementStatistics();
	
	}
	
}
