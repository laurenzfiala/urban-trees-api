package at.sparklingscience.urbantrees.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import at.sparklingscience.urbantrees.domain.UserAchievements;
import at.sparklingscience.urbantrees.domain.UserData;
import at.sparklingscience.urbantrees.domain.UserLevelAction;

/**
 * Mybatis mapping interface.
 * For user-related operations.
 * 
 * @author Laurenz Fiala
 * @since 2018/02/28, 2019/01/15
 */
@Mapper
public interface UserMapper {
	
	/**
	 * TODO
	 * @param phenologyId
	 * @param observationImg
	 */
	void insertPhenologyImage(
			@Param("phenologyId") int phenologyId,
			@Param("imgData") byte[] imgData,
			@Param("imgType") String imgType
			);

	/**
	 * TODO
	 * @param username
	 */
	UserAchievements findAchievementsForUserId(
			@Param("userId") int userId
			);

	/**
	 * TODO
	 * @param username
	 */
	int insertIncreaseLevel(
			@Param("xp") int xp,
			@Param("action") UserLevelAction action,
			@Param("userId") int userId			
			);
	
	void insertLevel(@Param("userId") int userId,
					 @Param("xp") int xp,
					 @Param("action") String action);
	
	UserData findUserData(@Param("userId") int userId);
	
}
