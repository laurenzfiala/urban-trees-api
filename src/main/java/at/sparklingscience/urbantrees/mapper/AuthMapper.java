package at.sparklingscience.urbantrees.mapper;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.http.HttpRequest;

import at.sparklingscience.urbantrees.domain.Role;
import at.sparklingscience.urbantrees.domain.User;
import at.sparklingscience.urbantrees.domain.UserIdentity;
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
	 * Find a user by their secure login key that was generated by an admin before.
	 * @param token secure login key
	 * @return the found user or null, if none was found
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
	 * @param username username (hashed)
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
	 * @param username Users' username (hashed).
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
	 * Find login key for user.
	 * @param userId user id
	 * @return secure login key if it exists and is not expired.
	 */
	String findUserLoginKey(@Param("userId") int userId);
	
	/**
	 * Update secure login key to given token.
	 * @param userId user id of user to update
	 * @param token secure login key to set (regardless of old key)
	 * @param tokenExpirationDate expiration date of given token
	 */
	void updateUserLoginKey(@Param("userId") int userId,
   		  					@Param("token") String token,
   		  					@Param("tokenExpirationDate") Date tokenExpirationDate);
	
	/**
	 * Insert a single user permission into access_data.user_permission
	 * @param grantingUserId User ID of permission granting user (giving permission)
	 * @param receivingUserId User ID of permission receiving user (getting permission)
	 * @param permission type of permission (as string)
	 */
	void insertUserPermission(@Param("grantingUserId") int grantingUserId,
							  @Param("receivingUserId") int receivingUserId,
							  @Param("permission") String permission);
	
	/**
	 * Check if receivingUser has the given permission granted by grantingUser.
	 * @param grantingUserId User ID of permission granting user (gave permission)
	 * @param receivingUserId User ID of permission receiving user (got permission)
	 * @param permission type of permission (as string)
	 * @return amount of active & matching user permissions
	 */
	Integer hasUserPermission(@Param("grantingUserId") int grantingUserId,
							  @Param("receivingUserId") int receivingUserId,
							  @Param("permission") String permission);
	
	/**
	 * Find all grantingUsers that the receivingUser has gotten the specified permission from.
	 * @param grantingUserId User ID of permission granting user (gave permission)
	 * @param receivingUserId User ID of permission receiving user (got permission)
	 * @param permission type of permission (as string)
	 * @return all {@link UserIdentity}s of granting users
	 */
	List<UserIdentity> findUserIdentitiesGrantingPermission(@Param("receivingUserId") int receivingUserId,
															@Param("permission") String permission);
	
	/**
	 * Find all receivingUsers that the receivingUser has given the specified permission to.
	 * @param grantingUserId User ID of permission granting user (gave permission)
	 * @param permission (optional) type of permission (as string); if null, all permissions are returned
	 * @return all {@link UserIdentity}s of granting users
	 */
	List<UserIdentity> findUserIdentitiesReceivingPermission(@Param("grantingUserId") int grantingUserId,
															 @Param("permission") String permission);

	/**
	 * Store the given PPIN in the database and reset the check tries.
	 * @param userId users' id
	 * @param ppin permissions PIN
	 */
	void setPermissionsPIN(@Param("userId") int userId, @Param("ppin") String ppin);
	
	/**
	 * Increase PPIN attempts by 1.
	 * @param userId users' id
	 */
	void increasePermissionsPINAttempts(@Param("userId") int userId);

	/**
	 * Whether the given user has the given PPIN or not.
	 * Might return false even if the PPIN is correct, when the
	 * attempts are exceeded.
	 * @param userId users' id
	 * @param ppin permissions PIN
	 * @return true if the users' ppin matches the given ppin
	 */
	boolean hasPermissionsPIN(@Param("userId") int userId, @Param("ppin") String ppin);
	
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
	
	/**
	 * Find the given users' token secret.
	 * @param userId users' id
	 * @return token secret
	 */
	String findUserTokenSecret(@Param("userId") int userId);
	
	/**
	 * Set the users' token secret.
	 * @param userId users' id
	 * @param tokenSecret Token secret in string form. The DB field has no limitation in size (text field).
	 * @return amount of rows affected (must be 1)
	 */
	int updateUserTokenSecret(@Param("userId") int userId, @Param("tokenSecret") String tokenSecret);
	
}
