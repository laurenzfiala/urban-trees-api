package at.sparklingscience.urbantrees.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.dao.DuplicateKeyException;

import at.sparklingscience.urbantrees.domain.PhysiognomyDataset;

/**
 * Mybatis mapping interface.
 * For physiognomy operations.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/27
 */
@Mapper
public interface PhysiognomyMapper {
	
	/**
	 * Get one or more physiognomy datasets, sorted by descending observation date.
	 * @param treeId ID of associated tree
	 * @param timespanMin Oldest date
	 * @param timespanMax Newest date
	 * @return All datasets found for the given tree.
	 */
	List<PhysiognomyDataset> findPhysiognomyByTreeId(
			@Param("treeId") long treeId,
			@Param("timespanMin") Date timespanMin,
			@Param("timespanMax") Date timespanMax
			);
	
	/**
	 * Store a single physiognomy dataset.
	 * @param dataset {@link PhysiognomyDataset} to insert.
	 * @throws DuplicateKeyException If an observation is already present for the given {@link PhysiognomyDataset#observationDate}.
	 */
	void insertPhysiognomyDataset(PhysiognomyDataset dataset) throws DuplicateKeyException;
	
}
