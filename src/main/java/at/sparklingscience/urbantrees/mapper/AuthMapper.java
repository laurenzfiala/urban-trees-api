package at.sparklingscience.urbantrees.mapper;

import java.util.List;
import java.util.UUID;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.http.HttpRequest;

import at.sparklingscience.urbantrees.security.AuthSettings;
import at.sparklingscience.urbantrees.security.apikey.ApiKeyFilter;
import at.sparklingscience.urbantrees.security.user.User;

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
	 * @return Amount of matching api keys.
	 */
	Integer hasValidApiKey(@Param("apiKey") UUID apiKey);
	
	/**
	 * Find a user by their username.
	 * @param username The username to find.
	 * @return The user found, or null.
	 */
	User findUserByUsername(@Param("username") String username);
	
	/**
	 * Insert a new user to the database.
	 * @param user Populated user DTO.
	 * @return The new user with populated ID.
	 */
	void insertUser(@Param("user") User user);
	
	/**
	 * Update the password of a user
	 * @return Number of affected users (can only be 0 or 1)
	 */
	int updateUserPassword(@Param("username") String username,
						   @Param("oldPassword") String oldPassword,
						   @Param("newPassword") String newPassword);
	
	/**
	 * Find all roles assigned to the given user.
	 * @param username username from the auth data
	 * @return list of roles/authorities
	 */
	List<String> findRolesForUser(@Param("username") String username);
	
	/**
	 * Find a single auth setting by key.
	 * @param key Settings' key.
	 * @return The settings' value.
	 */
	String findSetting(@Param("key") AuthSettings key);
	
}
