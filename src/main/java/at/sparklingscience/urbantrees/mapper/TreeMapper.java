package at.sparklingscience.urbantrees.mapper;

import org.apache.ibatis.annotations.Mapper;

import at.sparklingscience.urbantrees.domain.Tree;

/**
 * Mybatis mapping interface.
 * For tree operations.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/25
 */
@Mapper
public interface TreeMapper {
	
	Tree findTreeById(int id);
	
}
