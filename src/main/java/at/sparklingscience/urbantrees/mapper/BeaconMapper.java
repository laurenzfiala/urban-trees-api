package at.sparklingscience.urbantrees.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import at.sparklingscience.urbantrees.domain.Beacon;
import at.sparklingscience.urbantrees.domain.BeaconDataset;
import at.sparklingscience.urbantrees.domain.BeaconLog;
import at.sparklingscience.urbantrees.domain.BeaconLogSeverity;
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
	
	List<Beacon> findAllBeaconsActive();
	
	List<Beacon> findAllBeaconsByStatus(@Param("status") BeaconStatus status);
	
	Beacon findBeaconById(@Param("beaconId") long beaconId);
	
	Beacon findBeaconByAddress(@Param("address") String address);
	
	List<BeaconDataset> findBeaconDataByBeaconId(
			@Param("beaconId") long beaconId,
			@Param("maxDatapoints") int maxDatapoints,
			@Param("timespanMin") Date timespanMin,
			@Param("timespanMax") Date timespanMax
			);
	
	void insertBeaconDatasets(
			@Param("beaconId") long beaconId,
			@Param("datasets") List<BeaconDataset> datasets
			);
	
	BeaconSettings findLatestBeaconSettingsByBeaconId(
			@Param("beaconId") long beaconId
			);
	
	List<BeaconLog> findBeaconLogs(
			@Param("beaconId") Long beaconId,
			@Param("severities") List<BeaconLogSeverity> severities,
			@Param("offset") Integer offset,
			@Param("maxLogs") Integer maxLogs,
			@Param("timespanMin") Date timespanMin,
			@Param("timespanMax") Date timespanMax
			);
	
	void insertBeaconSettings(
			@Param("beaconId") long beaconId,
			@Param("settings") BeaconSettings settings,
			@Param("user") String user
			);
	
	void insertBeaconLog(
			@Param("beaconId") long beaconId,
			@Param("log") BeaconLog log
			);
	
	void insertBeaconLogs(
			@Param("logs") List<BeaconLog> logs
			);
	
	void insertBeacon(
			@Param("beacon") Beacon beacon,
			@Param("user") String user
			);
	
	void updateBeacon(
			@Param("beacon") Beacon beacon,
			@Param("user") String user
			);

	void updateBeaconStatus(
			@Param("id") int id,
			@Param("status") BeaconStatus status
			);
	
	void updateBeaconSettings(
			@Param("settings") BeaconSettings settings,
			@Param("user") String user
			);
	
}
