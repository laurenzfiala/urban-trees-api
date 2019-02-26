package at.sparklingscience.urbantrees.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import at.sparklingscience.urbantrees.domain.City;
import at.sparklingscience.urbantrees.domain.Tree;
import at.sparklingscience.urbantrees.domain.TreeLocation;
import at.sparklingscience.urbantrees.domain.TreeSpecies;

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

	void insertTreeLocation(@Param("location") TreeLocation location, @Param("user") String user);

	void insertTreeAge(@Param("tree") Tree tree, @Param("user") String user);
	
	void insertTree(@Param("tree") Tree tree, @Param("user") String user);
	
	void updateTree(@Param("tree") Tree tree, @Param("user") String user);
	
	Tree findTreeById(int id);
	
	int findSpeciesIdForTreeId(int treeId);
	
	List<City> getCities();
	
	void insertCity(@Param("city") City city, @Param("user") String user);
	
	List<TreeSpecies> getSpecies();
	
}
