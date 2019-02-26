package at.sparklingscience.urbantrees.mapper;

import java.util.List;
import java.util.UUID;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.http.HttpRequest;

import at.sparklingscience.urbantrees.domain.Role;
import at.sparklingscience.urbantrees.domain.User;
import at.sparklingscience.urbantrees.domain.UserLight;
import at.sparklingscience.urbantrees.security.AuthSettings;
import at.sparklingscience.urbantrees.security.apikey.ApiKeyFilter;

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
	 * TODO
	 * @param token
	 * @return
	 */
	User findUserByLoginKey(@Param("token") String token);
	
	/**
	 * Find a user by their id.
	 * @param userId id of the user to find
	 * @return The user found, or null.
	 */
	User findUserById(@Param("userId") int userId);
	
	/**
	 * Find a user by their username.
	 * @param username username
	 * @return The user found, or null.
	 */
	User findUserByUsername(@Param("username") String username);
	
	/**
	 * Find all granted roles for the specified user.
	 * @param id id of the user to look for
	 * @return collection of roles granted to the given user
	 */
	List<String> findRolesByUserId(@Param("id") int id);
	
	/**
	 * Find all existing users.
	 * @return List of users.
	 */
	List<UserLight> findAllUsersLight();
	
	/**
	 * Insert a new user to the database.
	 * Does not insert roles, use #insertUserRoles for that.
	 * @param user Populated user DTO.
	 * @return The new user with populated ID.
	 */
	void insertUser(@Param("user") User user);
	
	/**
	 * Insert roles for given user.
	 * @param userId id of user
	 * @param roles roles to be assigned to user.
	 */
	void insertUserRoles(@Param("userId") int userId, @Param("roles") List<Role> roles);
	
	/**
	 * Remove specified roles from given user.
	 * @param userId id of the user
	 * @param roles role to remove
	 */
	void deleteUserRoles(@Param("userId") int userId, @Param("roles") List<Role> roles);
	
	/**
	 * Delete a single user form the database.
	 * @param userId User's id.
	 */
	void deleteUser(@Param("userId") int userId);

	/**
	 * Update the given users' last login attempt date
	 * to the current time.
	 * @param username Username of user to update.
	 */
	void updateLastLoginAttemptDatByUsername(@Param("username") String username);
	
	/**
	 * Update last login date to current time.
	 * @param userId Users' id.
	 */
	void updateLastLoginDat(@Param("userId") int userId);
	
	/**
	 * Increase failed user login attempts by 1 and if 
	 * the attempts is equal or greater to MAX_LOGIN_ATTEMPTS in
	 * access_data.settings, set credentials_non_expired to true.
	 * @param username Users' username.
	 */
	void increaseFailedLoginAttemptsByUsername(@Param("username") String username);

	/**
	 * Reset failed login attempts upon successful login.
	 * @param userId Users' id.
	 */
	void resetFailedLoginAttempts(@Param("userId") int userId);
	
	/**
	 * Update the password of a user and set credentials to non expired.
	 * @param userId users' id.
	 * @param newPassword New password to set.
	 * @return Number of affected users (can only be 0 or 1)
	 */
	int updateUserPassword(@Param("userId") int userId,
						   @Param("newPassword") String newPassword);

	/**
	 * Update a users' username.
	 * @param userId users' id
	 * @param newUsername new username
	 * @return Number of affected users (can only be 0 or 1)
	 */
	int updateUsername(@Param("userId") int userId,
					   @Param("newUsername") String newUsername);

	/**
	 * Expire credentials of given user or set them un-expired.
	 * @param userId Users' id.
	 * @param nonExipred true if you want to un-expire them; false to expire
	 */
	void updateCredentialsNonExpired(@Param("userId") int userId,
								     @Param("nonExpired") boolean nonExipred);
	
	/**
	 * Set active property of given user.
	 * @param userId Users' id
	 * @param active true to set active; false otherwise
	 */
	void updateActive(@Param("userId") int userId,
		     		  @Param("active") boolean active);
	
	/**
	 * TODO
	 * @param userId
	 */
	String findUserLoginKey(@Param("userId") int userId);
	
	/**
	 * TODO
	 * @param userId
	 * @param token
	 */
	void updateUserLoginKey(@Param("userId") int userId,
   		  					@Param("token") String token);
	
	/**
	 * Find all available users.
	 * @return list of roles
	 */
	List<Role> findAllUserRoles();
	
	/**
	 * Find a single auth setting by key.
	 * @param key Settings' key.
	 * @return The settings' value.
	 */
	String findSetting(@Param("key") AuthSettings key);
	
}
