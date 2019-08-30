package at.sparklingscience.urbantrees.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import at.sparklingscience.urbantrees.domain.Event;
import at.sparklingscience.urbantrees.domain.EventSeverity;
import at.sparklingscience.urbantrees.domain.Report;
import at.sparklingscience.urbantrees.domain.UserIdentity;

/**
 * Mybatis mapping interface.
 * For application-schema-related operations like logging and reports.
 * 
 * @author Laurenz Fiala
 * @since 2019/07/27
 */
@Mapper
public interface ApplicationMapper {
	
	List<Event> findEvents(
			@Param("id") Integer id,
			@Param("severities") List<EventSeverity> severities,
			@Param("offset") Integer offset,
			@Param("maxEvents") Integer maxEvents,
			@Param("timespanMin") Date timespanMin,
			@Param("timespanMax") Date timespanMax
			);
	
	void insertEvent(@Param("event") Event event);
	
	List<Report> findReports(
			@Param("id") Integer id,
			@Param("autoCreate") Boolean autoCreate,
			@Param("resolved") Boolean resolved,
			@Param("offset") Integer offset,
			@Param("maxReports") Integer maxReports,
			@Param("timespanMin") Date timespanMin,
			@Param("timespanMax") Date timespanMax
			);
	
	List<Report> findUserReports(@Param("userId") int userId);
	
	void insertReport(@Param("report") Report report);
	
	void updateReportRemark(
			@Param("id") int id,
			@Param("remark") String remark
			);
	
	void updateReportResolved(
			@Param("id") int id,
			@Param("resolved") boolean resolved
			);
	
	int nextUserRefId();
	
	void insertUserRef(
			@Param("refId") int refId,
			@Param("userId") int userId
			);
	
	List<UserIdentity> findUsersByRefId(
			@Param("refId") int refId
			);
	
	List<Integer> findUserIdsByRefId(
			@Param("refId") int refId
			);
	
}
