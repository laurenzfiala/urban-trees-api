package at.sparklingscience.urbantrees.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import at.sparklingscience.urbantrees.domain.UserContent;

/**
 * Mybatis mapping interface.
 * For user-content-related operations.
 * 
 * @author Laurenz Fiala
 * @since 2018/03/13
 */
@Mapper
public interface UserContentMapper {

	/**
	 * 
	 * @param userId
	 * @param contentId
	 * @return
	 */
	UserContent findContent(
			@Param("userId") int userId,
			@Param("id") int contentId
			);
	
}
