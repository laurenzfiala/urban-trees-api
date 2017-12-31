package at.sparklingscience.urbantrees.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import at.sparklingscience.urbantrees.domain.Beacon;
import at.sparklingscience.urbantrees.domain.BeaconDataset;

/**
 * Mybatis mapping interface.
 * For physiognomy operations.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/27
 */
@Mapper
public interface BeaconMapper {
	
	Beacon findBeaconById(@Param("beaconId") int beaconId);
	
	Beacon findBeaconByAddress(@Param("address") String address);
	
	List<BeaconDataset> findBeaconDataByBeaconId(
			@Param("beaconId") int beaconId,
			@Param("timespanMin") Date timespanMin,
			@Param("timespanMax") Date timespanMax
			);
	
	void insertBeaconDataset(BeaconDataset dataset);
	
}
