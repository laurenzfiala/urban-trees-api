package at.sparklingscience.urbantrees.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import at.sparklingscience.urbantrees.domain.PhenologyDataset;
import at.sparklingscience.urbantrees.domain.PhenologyDatasetWithTree;
import at.sparklingscience.urbantrees.domain.PhenologyObservationType;

/**
 * Mybatis mapping interface.
 * For phenology operations.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/30
 */
@Mapper
public interface PhenologyMapper {
	
	List<PhenologyDataset> findPhenologyByTreeId(
			@Param("treeId") long treeId,
			@Param("timespanMin") Date timespanMin,
			@Param("timespanMax") Date timespanMax
			);
	
	PhenologyDataset findPhenologyById(
			@Param("phenologyId") long phenologyId
			);
	
	List<PhenologyDatasetWithTree> findPhenologyByUserId(
			@Param("userId") int userId,
			@Param("limit") int limit
			);
	
	void insertPhenology(PhenologyDataset dataset);
	
	void insertPhenologyObservation(PhenologyDataset dataset);
	
	List<PhenologyObservationType> getObservationTypesForTreeSpeciesId(int treeSpeciesId);
	
	List<PhenologyObservationType> getAllObservationTypes();
	
}
