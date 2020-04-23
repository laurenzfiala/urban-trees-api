package at.sparklingscience.urbantrees.domain;

/**
 * Credentials for user-OTP.
 * Holds the OTP secret and scratchCodes.
 * 
 * @author Laurenz Fiala
 * @since 2020/04/22
 */
public class OtpCredentials {

	private String secret;

	private String scratchCode1;
	private String scratchCode2;
	private String scratchCode3;
	
	public OtpCredentials() {
	}
	
	public OtpCredentials(String secret, String[] scratchCodes) {
		this.secret = secret;
		this.scratchCodes(scratchCodes);
	}
	
	public String[] getScratchCodes() {
		return new String[] {this.scratchCode1, this.scratchCode2, this.scratchCode3};
	}
	
	/**
	 * Sets all scratch codes and returns <code>this</code> instance.
	 */
	public OtpCredentials scratchCodes(String[] scratchCodes) {
		this.scratchCode1 = scratchCodes[0];
		this.scratchCode2 = scratchCodes[1];
		this.scratchCode3 = scratchCodes[2];
		return this;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getScratchCode1() {
		return scratchCode1;
	}

	public void setScratchCode1(String scratchCode1) {
		this.scratchCode1 = scratchCode1;
	}

	public String getScratchCode2() {
		return scratchCode2;
	}

	public void setScratchCode2(String scratchCode2) {
		this.scratchCode2 = scratchCode2;
	}

	public String getScratchCode3() {
		return scratchCode3;
	}

	public void setScratchCode3(String scratchCode3) {
		this.scratchCode3 = scratchCode3;
	}

}
