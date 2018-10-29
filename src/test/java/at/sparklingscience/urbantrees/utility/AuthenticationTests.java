package at.sparklingscience.urbantrees.utility;

import java.io.IOException;

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
	
	@Test
	@Commit
	public void registerUser() throws ClientProtocolException, IOException {
		
		this.authenticationService.registerUser("test", "test");
		
	}

}
