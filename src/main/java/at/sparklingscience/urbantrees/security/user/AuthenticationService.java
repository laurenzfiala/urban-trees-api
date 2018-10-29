package at.sparklingscience.urbantrees.security.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import at.sparklingscience.urbantrees.mapper.AuthMapper;

/**
 * Service provides functionalits for authorization,
 * e.g. register users, etc.
 * 
 * @author Laurenz Fiala
 * @since 2018/06/10
 */
@Service
public class AuthenticationService {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private AuthMapper authMapper;
	
	/**
	 * Searches for a user with the given username.
	 * @param username The username to look for.
	 * @return If the user is found, return that user; if not, null will be returned.
	 */
	@Transactional
	public User findUser(final String username) {
		
		return this.authMapper.findUserByUsername(username);
		
	}
	
	/**
	 * Inserts a new user into the database.
	 * @param username Username to register.
	 * @param rawPassword Password entered by the user (raw).
	 * @return The given user object with ID set.
	 */
	@Transactional
	public User registerUser(final String username, final String rawPassword) {
		
		User newUser = new User();
		newUser.setUsername(username);
		
		final String hashedPassword = this.bCryptPasswordEncoder.encode(rawPassword);
		newUser.setPassword(hashedPassword);
		
		this.authMapper.insertUser(newUser);
		
		return newUser;
		
	}

	/**
	 * Update a users' password to newPassword
	 * if oldPassword matches the previously stored password.
	 * @param username Username of user to update
	 * @param oldPassword Previous password
	 * @param newPassword New password to be stored
	 * @return true if the password change was successful, false otherwise
	 */
	@Transactional
	public boolean changePassword(final String username, final String oldPassword, final String newPassword) {
		
		final String hashedOldPassword = this.bCryptPasswordEncoder.encode(oldPassword);
		final String hashedNewPassword = this.bCryptPasswordEncoder.encode(newPassword);
		
		final int updatedRows = this.authMapper.updateUserPassword(username, hashedOldPassword, hashedNewPassword);
		
		return updatedRows == 1;
		
	}
	
}
