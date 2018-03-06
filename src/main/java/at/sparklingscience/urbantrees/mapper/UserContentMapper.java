package at.sparklingscience.urbantrees.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Mybatis mapping interface.
 * For user-generated content upload operations.
 * 
 * @author Laurenz Fiala
 * @since 2018/02/28
 */
@Mapper
public interface UserContentMapper {
	
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
	
}
