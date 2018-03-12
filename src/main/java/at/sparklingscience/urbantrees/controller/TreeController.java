package at.sparklingscience.urbantrees.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
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
import at.sparklingscience.urbantrees.domain.PhenologyDataset;
import at.sparklingscience.urbantrees.domain.PhenologyObservationType;
import at.sparklingscience.urbantrees.domain.PhysiognomyDataset;
import at.sparklingscience.urbantrees.domain.Tree;
import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.ClientError;
import at.sparklingscience.urbantrees.exception.InternalException;
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
	
	@RequestMapping(method = RequestMethod.GET, path = "")
	public List<Tree> getAllTrees() {
		
		LOGGER.debug("[[ GET ]] getAllTrees");
		return this.treeMapper.getAllTrees();
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/{treeId:\\d+}")
	public Tree getTree(@PathVariable int treeId) {
		
		LOGGER.debug("[[ GET ]] getTree - treeId: {}", treeId);
		
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
		
		LOGGER.debug("[[ GET ]] getTreePhysiognomy - treeId: {}", treeId);
		
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
	public PhysiognomyDataset postTreePhysiognomyDataset(
			@PathVariable int treeId,
			@Validated(ValidationGroups.Update.class) @RequestBody PhysiognomyDataset dataset) {
		
		LOGGER.info("[[ POST ]] postTreePhysiognomyDataset - treeId: {}", treeId);
		
		if (treeId != dataset.getTreeId()) {
			throw new BadRequestException("Datasets' tree id does not match the paths' tree id.");
		}
		
		try {
			this.physiognomyMapper.insertPhysiognomyDataset(dataset);
		} catch (DuplicateKeyException ex) {
			LOGGER.debug("User tried to enter duplicate key: {}", ex.getMessage(), ex);
			throw new BadRequestException("There is already an observation with given observationDate.", ClientError.PHENOLOGY_DUPLICATE);
		}
		
		LOGGER.info("[[ POST ]] postTreePhysiognomyDataset |END| - treeId: {}, inserted dataset id: {}", treeId, dataset.getId());
		
		return dataset;
		
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/{treeId:\\d+}/phenology")
	public List<PhenologyDataset> getTreePhenology(
			@PathVariable int treeId,
			@RequestParam(required = false) String timespanMin,
			@RequestParam(required = false) String timespanMax) {
		
		LOGGER.debug("[[ GET ]] getTreePhenology - treeId: {}", treeId);
		
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
	
	@RequestMapping(method = RequestMethod.GET, path = "/{speciesId:\\d+}/phenology/spec")
	public List<PhenologyObservationType> getPhenologyObservationSpecForSpeciesId(@PathVariable int speciesId) {
		
		LOGGER.debug("[[ GET ]] getPhenologyObservationSpecForSpeciesId - speciesId: {}", speciesId);
		
		final List<PhenologyObservationType> spec = this.phenologyMapper.getObservationTypesForTreeSpeciesId(speciesId);
		
		if (spec == null) {
			throw new InternalException("Could not find the phenology observation specification for species id " + speciesId + ".");
		}
		
		return spec;
		
	}
	
	@Transactional
	@RequestMapping(method = RequestMethod.POST, path = "/{treeId:\\d+}/phenology")
	public PhenologyDataset postTreePhenologyDataset(
			@PathVariable int treeId,
			@Validated(ValidationGroups.Update.class) @RequestBody PhenologyDataset dataset) {
		
		LOGGER.info("[[ POST ]] postTreePhenologyDataset - treeId: {}", treeId);
		
		if (treeId != dataset.getTreeId()) {
			throw new BadRequestException("Datasets' tree id does not match the paths' tree id.");
		}
		
		try {
			this.phenologyMapper.insertPhenology(dataset);
			this.phenologyMapper.insertPhenologyObservation(dataset);
		} catch (DuplicateKeyException ex) {
			LOGGER.debug("User tried to enter duplicate key: {}", ex.getMessage(), ex);
			throw new BadRequestException("There is already an observation with given observationDate.", ClientError.PHENOLOGY_DUPLICATE);
		}
		
		LOGGER.info("[[ POST ]] postTreePhenologyDataset |END| - treeId: {}, inserted dataset id: {}", treeId, dataset.getId());
		
		return dataset;
		
	}

}
