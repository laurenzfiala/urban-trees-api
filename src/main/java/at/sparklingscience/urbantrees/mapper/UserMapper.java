package at.sparklingscience.urbantrees.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import at.sparklingscience.urbantrees.domain.UserAchievements;
import at.sparklingscience.urbantrees.domain.UserData;
import at.sparklingscience.urbantrees.domain.UserLevelAction;
import at.sparklingscience.urbantrees.domain.UserLevelActionContext;
import at.sparklingscience.urbantrees.domain.UserXp;

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
	 * Find complete {@link UserAchievements} with all XP entries.
	 * @param userId id of user to find
	 */
	UserAchievements findAchievementsForUserId(
			@Param("userId") int userId
			);

	/**
	 * Find all stored XP entries for th egiven user id.
	 * @param userId id of user
	 */
	List<UserXp> findXpHistoryByUserId(
			@Param("userId") int userId
			);

	/**
	 * Insert new level action and increase the previous XP amount by the given xp.
	 * @param xp xp to increase by
	 * @param action {@link UserLevelAction}
	 * @param userId affected user id
	 * @param context context of why level action occurred
	 * @return amount of inserted rows (0 if failed, 1 success)
	 */
	int insertIncreaseLevel(
			@Param("xp") int xp,
			@Param("action") UserLevelAction action,
			@Param("userId") int userId,
			@Param("context") UserLevelActionContext context
			);
	
	void insertLevel(@Param("userId") int userId,
					 @Param("xp") int xp,
					 @Param("action") String action,
					 @Param("context") UserLevelActionContext context);
	
	UserData findUserData(@Param("userId") int userId);
	
}
