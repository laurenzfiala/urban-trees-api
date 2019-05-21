package at.sparklingscience.urbantrees.mapper;

import java.util.List;

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
	 * TODO
	 * @param username
	 */
	List<UserContent> findContent(
			@Param("id") Integer id,
			@Param("tag") String tag
			);
	
}
