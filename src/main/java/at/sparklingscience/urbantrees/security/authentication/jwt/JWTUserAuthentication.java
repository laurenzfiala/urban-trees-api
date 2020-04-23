package at.sparklingscience.urbantrees.security.authentication.jwt;

import javax.crypto.SecretKey;

import io.jsonwebtoken.io.Encoders;

/**
 * A user authentication session.
 * 
 * @author Laurenz Fiala
 * @since 2020/04/22
 */
public class JWTUserAuthentication {
	
	private long id;
	
	private int userId;

	private SecretKey secret;
	
	public JWTUserAuthentication(int userId, SecretKey secret) {
		this.setUserId(userId);
		this.secret = secret;
	}
	
	/**
	 * Return base64 encoded secret for persistence.
	 * @return
	 */
	public String getSecretAsString() {
		return Encoders.BASE64.encode(this.secret.getEncoded());
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public SecretKey getSecret() {
		return secret;
	}

	public void setSecret(SecretKey secret) {
		this.secret = secret;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
	
}
