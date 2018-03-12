package at.sparklingscience.urbantrees.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
	
	List<Tree> getAllTrees();
	
	Tree findTreeById(int id);
	
	int getSpeciesIdForTreeId(int treeId);
	
}
