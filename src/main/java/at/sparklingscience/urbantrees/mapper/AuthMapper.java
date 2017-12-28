package at.sparklingscience.urbantrees.mapper;

import java.util.UUID;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.http.HttpRequest;

import at.sparklingscience.urbantrees.security.ApiKeyFilter;

/**
 * Mybatis mapping interface.
 * For authorization operations.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/26
 */
@Mapper
public interface AuthMapper {
	
	/**
	 * Whether the given api key is a valid entry in the database.
	 * @param apiKey api key from {@link HttpRequest}, see {@link ApiKeyFilter}.
	 * @return true if api key exists in the db; false otherwise.
	 */
	Integer hasValidApiKey(@Param("apiKey") UUID apiKey);
	
}
