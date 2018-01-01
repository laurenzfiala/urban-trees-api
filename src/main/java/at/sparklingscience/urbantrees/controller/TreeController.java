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
import at.sparklingscience.urbantrees.domain.PhenologyDataset;
import at.sparklingscience.urbantrees.domain.PhysiognomyDataset;
import at.sparklingscience.urbantrees.domain.Tree;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.NotFoundException;
import at.sparklingscience.urbantrees.mapper.PhenologyMapper;
import at.sparklingscience.urbantrees.mapper.PhysiognomyMapper;
import at.sparklingscience.urbantrees.mapper.TreeMapper;

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
	
	@Autowired
    private PhenologyMapper phenologyMapper;
	
	@Value("${at.sparklingscience.urbantrees.dateFormatPattern}")
	private String dateFormatPattern;
	
	@RequestMapping(method = RequestMethod.GET, path = "/{treeId:\\d+}")
	public Tree getTree(@PathVariable int treeId) {
		
		Tree tree = this.treeMapper.findTreeById(treeId);
		if (tree == null) {
			throw new NotFoundException("No tree found for given id.");
		}
		
		return tree;
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/{treeId:\\d+}/physiognomy")
	public List<PhysiognomyDataset> getTreePhysiognomy(
			@PathVariable int treeId,
			@RequestParam(required = false) String timespanMin,
			@RequestParam(required = false) String timespanMax) {
		
		Timespan timespan = ControllerUtil.getTimespanParams(this.dateFormatPattern, timespanMin, timespanMax);
		
		List<PhysiognomyDataset> datasets = 
				this.physiognomyMapper.findPhysiognomyByTreeId(
					treeId,
					timespan.getStart(),
					timespan.getEnd()
					);
	
		if (datasets == null) {
			throw new NotFoundException("No tree found for given id.");
		}
		
		return datasets;
		
	}
	
	@Transactional
	@RequestMapping(method = RequestMethod.POST, path = "/{treeId:\\d+}/physiognomy")
	public void putTreePhysiognomyDataset(@PathVariable int treeId, @RequestBody PhysiognomyDataset dataset) {
		
		if (treeId != dataset.getTreeId()) {
			throw new BadRequestException("Datasets' tree id does not match the paths' tree id.");
		}
		
		this.physiognomyMapper.insertPhysionomyDataset(dataset);
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/{treeId:\\d+}/phenology")
	public List<PhenologyDataset> getTreePhenology(
			@PathVariable int treeId,
			@RequestParam(required = false) String timespanMin,
			@RequestParam(required = false) String timespanMax) {
		
		Timespan timespan = ControllerUtil.getTimespanParams(this.dateFormatPattern, timespanMin, timespanMax);

		List<PhenologyDataset> datasets = 
				this.phenologyMapper.findPhenologyByTreeId(
						treeId,
						timespan.getStart(),
						timespan.getEnd()
						);

		if (datasets == null) {
			throw new NotFoundException("No tree found for given id.");
		}

		return datasets;
		
	}
	
	@Transactional
	@RequestMapping(method = RequestMethod.POST, path = "/{treeId:\\d+}/phenology")
	public void putTreePhenologyDataset(@PathVariable int treeId, @RequestBody PhenologyDataset dataset) {
		
		if (treeId != dataset.getTreeId()) {
			throw new BadRequestException("Datasets' tree id does not match the paths' tree id.");
		}
		
		this.phenologyMapper.insertPhenology(dataset);
		this.phenologyMapper.insertPhenologyObservation(dataset);
		
	}
	


}
