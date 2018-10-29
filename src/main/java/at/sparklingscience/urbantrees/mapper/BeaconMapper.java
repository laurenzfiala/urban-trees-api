package at.sparklingscience.urbantrees.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import at.sparklingscience.urbantrees.domain.Beacon;
import at.sparklingscience.urbantrees.domain.BeaconDataset;
import at.sparklingscience.urbantrees.domain.BeaconLog;
import at.sparklingscience.urbantrees.domain.BeaconSettings;
import at.sparklingscience.urbantrees.domain.BeaconStatus;

/**
 * Mybatis mapping interface.
 * For physiognomy operations.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/27
 */
@Mapper
public interface BeaconMapper {
	
	List<Beacon> findAllBeaconsByStatus(@Param("status") BeaconStatus status);
	
	Beacon findBeaconById(@Param("beaconId") int beaconId);
	
	Beacon findBeaconByAddress(@Param("address") String address);
	
	List<BeaconDataset> findBeaconDataByBeaconId(
			@Param("beaconId") int beaconId,
			@Param("timespanMin") Date timespanMin,
			@Param("timespanMax") Date timespanMax
			);
	
	void insertBeaconDatasets(
			@Param("beaconId") int beaconId,
			@Param("datasets") List<BeaconDataset> datasets
			);
	
	BeaconSettings findLatestBeaconSettingsByBeaconId(
			@Param("beaconId") int beaconId
			);
	
	void insertBeaconSettings(
			@Param("beaconId") int beaconId,
			@Param("settings") BeaconSettings settings
			);
			
	void insertBeaconLogs(
			@Param("beaconId") int beaconId,
			@Param("logs") List<BeaconLog> logs
			);
	
}
