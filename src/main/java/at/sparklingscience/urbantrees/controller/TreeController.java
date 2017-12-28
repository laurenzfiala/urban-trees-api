package at.sparklingscience.urbantrees.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import at.sparklingscience.urbantrees.domain.PhysiognomyDataset;
import at.sparklingscience.urbantrees.domain.Tree;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.InternalException;
import at.sparklingscience.urbantrees.exception.NotFoundException;
import at.sparklingscience.urbantrees.mapper.PhysiognomyMapper;
import at.sparklingscience.urbantrees.mapper.TreeMapper;
import at.sparklingscience.urbantrees.security.ApiKeyFilter;

@RestController
@RequestMapping("/tree")
public class TreeController {
	
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TreeController.class);
	
	@Autowired
	private TreeMapper treeMapper;
	
	@Autowired
    private PhysiognomyMapper physiognomyMapper;
	
	@Value("${at.sparklingscience.urbantrees.dateFormatPattern}")
	private String dateFormatPattern;
	
	@RequestMapping(method = RequestMethod.GET, path = "/{treeId:\\d+}")
	Tree getTree(@PathVariable String treeId) {
		
		Tree tree = this.treeMapper.findTreeById(Integer.parseInt(treeId));
		if (tree == null) {
			throw new NotFoundException("No tree found for given id.");
		}
		
		return tree;
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/{treeId:\\d+}/physiognomy")
	List<PhysiognomyDataset> getTreePhysiognomy(
			@PathVariable String treeId,
			@RequestParam(name = "timespan_min", required = false) String timespanMin,
			@RequestParam(name = "timespan_max", required = false) String timespanMax) {
		
		try {
			
			DateFormat dateFormat = new SimpleDateFormat(this.dateFormatPattern);
			
			Date timespanMinDate = null;
			if (timespanMin != null) {
				timespanMinDate = dateFormat.parse(timespanMin);
				LOGGER.debug("Using timespan min of {}", timespanMin);
			}
			
			Date timespanMaxDate = null;
			if (timespanMax != null) {
				timespanMaxDate = dateFormat.parse(timespanMax);
				LOGGER.debug("Using timespan max of {}", timespanMax);
			}
			
			List<PhysiognomyDataset> datasets = 
					this.physiognomyMapper.findPhysiognomyByTreeId(
						Integer.parseInt(treeId),
						timespanMinDate,
						timespanMaxDate
						);
		
			if (datasets == null) {
				throw new NotFoundException("No tree found for given id.");
			}
			
			return datasets;
			
		} catch (ParseException e) {
			LOGGER.error("Timestamps could not be parsed: " + e.getMessage(), e);
			throw new BadRequestException("Timestamps could not be parsed.");
		}
		
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/{treeId:\\d+}/physiognomy")
	void putTreePhysiognomyDataset(@PathVariable String treeId, @RequestBody PhysiognomyDataset dataset) {
		
		this.physiognomyMapper.insertPhysionomyDataset(dataset);
		
	}

}
