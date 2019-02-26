package at.sparklingscience.urbantrees.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import at.sparklingscience.urbantrees.domain.ui.Announcement;
import at.sparklingscience.urbantrees.domain.ui.Image;
import at.sparklingscience.urbantrees.domain.ui.Statistics;

/**
 * Mybatis mapping interface.
 * For UI operations.
 * 
 * @author Laurenz Fiala
 * @since 2018/02/04
 */
@Mapper
public interface UiMapper {
	
	/**
	 * Gets image data for the phenology observation results from the db.
	 * @param treeSpeciesId The trees' species id.
	 * @param resultId The observation result id for which to retrieve the image data.
	 * @return {@link Image}
	 */
	Image findImageForPhenologyObservationResult(
			@Param("treeSpeciesId") int treeSpeciesId,
			@Param("resultId") int resultId
			);
	
	/**
	 * Gets all current announcements to show ordered by
	 * severity.
	 * @return list of current announcements
	 */
	List<Announcement> getCurrentAnnouncements();
	
	/**
	 * Gets all announcements ordered by 
	 * @return list of announcements
	 */
	List<Announcement> getAllAnnouncements();
	
	/**
	 * Insert a single announcment into the db.
	 */
	void insertAnnouncement(@Param("a") Announcement announcement,
							@Param("user") String user);
	
	/**
	 * Delete a single announcement form the db.
	 */
	void deleteAnnouncement(@Param("aId") int announcementId);
	
	/**
	 * Gets statistics on the system.
	 * @return {@link Statistics}
	 */
	Statistics getStatistics();
	
}
