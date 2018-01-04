package at.sparklingscience.urbantrees.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
	
	List<PhysiognomyDataset> findPhysiognomyByTreeId(
			@Param("treeId") int treeId,
			@Param("timespanMin") Date timespanMin,
			@Param("timespanMax") Date timespanMax
			);
	
	
	void insertPhysiognomyDataset(PhysiognomyDataset dataset);
	
}
