package at.sparklingscience.urbantrees.utility;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import at.sparklingscience.urbantrees.mapper.AuthMapper;
import at.sparklingscience.urbantrees.security.user.AuthenticationService;

/**
 * Utility tests to execute some logic for internal use.
 * 
 * @author Laurenz Fiala
 * @since 2018/06/10
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@ContextConfiguration
public class AuthenticationTests {
	
	@Autowired
	private AuthenticationService authenticationService;
	
	@Autowired
	private AuthMapper authMapper;
	
	@Test
	@Commit
	public void registerUser() throws ClientProtocolException, IOException {
		
		this.authenticationService.registerUser("test", "test", new ArrayList<>());
		
	}
	
	/*
	@Test
	@Commit
	public void encryptAllPlainUsernames() throws ClientProtocolException, IOException {
		
		for (UserLight u : this.authMapper.findAllUsersLight()) {
			this.authenticationService.changeUsername(u.getId(), u.getUsername());
			System.out.println("===== SUCCESSFULLY UPDATED USER " + u.getUsername() + " =====");
		}
		
	}
	
	@Test
	@Commit
	public void reencryptAllUsernames() throws ClientProtocolException, IOException {
		
		for (UserLight u : this.authenticationService.getAllUsersLight()) {
			this.authenticationService.changeUsername(u.getId(), u.getUsername());
			System.out.println("===== SUCCESSFULLY UPDATED USER " + u.getUsername() + " =====");
		}
		
	}
	
	@Test
	public void encryptUsernameManual() throws ClientProtocolException, IOException {
		
		User user = this.authMapper.findUserById(31);
		System.out.println(cryptor.encrypt(user.getUsername()));
		
	}
	
	@Test
	public void getRandomHexStringForEncSalt() throws ClientProtocolException, IOException {
		
		System.out.println(KeyGenerators.string().generateKey());
		
	}
	*/

}
