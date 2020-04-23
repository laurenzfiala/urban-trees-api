package at.sparklingscience.urbantrees.security.authentication.otp;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

/**
 * Handles time-based one-time passwords.
 * Generates secret and verifies passwords.
 * 
 * @author Laurenz Fiala
 * @since 2020/04/22
 */
public class Totp {
	
	/**
	 * Holds the TOTP secret.
	 */
	private final byte[] secret;
	
	/**
	 * Holds the scratch codes.
	 */
	private final String[] scratchCodes;
	
	/**
	 * Generate a new TOTP secret and scratch codes.
	 */
	public Totp() {		
		this.secret = this.generateSecret();
		this.scratchCodes = this.generateScratchCodes();
	}
	
	/**
	 * Populate the TOTP with the given secret and scratch codes.
	 * This should be used for existing users.
	 * @param secret the base32 encoded secret (output of {@link #secret()})
	 * @param scratchCodes users' scratch codes
	 */
	public Totp(String secret, String[] scratchCodes) {
		this.secret = new Base32().decode(secret);
		this.scratchCodes = scratchCodes;
	}
	
	/**
	 * Return the associated TOTP secret.
	 */
	public String secret() {
		
		return new Base32().encodeAsString(this.secret);
		
	}
	
	/**
	 * Return the associated scratch codes.
	 */
	public String[] scratchCodes() {
		
		return this.scratchCodes;
		
	}
	
	/**
	 * Verify the given TOTP code with the scratch codes as fallback.
	 * If any of the scratch codes were used, {@link #scratchCodes()} will return
	 * null in place of it in the array.
	 * Therefore, to make sure scratch codes can only be used once, persist {@link #scratchCodes()}
	 * after calling {@link #verify(String)}.
	 * 
	 * It is guaranteed that an {@link OtpValidationException} is thrown by this method if
	 * validation fails.
	 * 
	 * @param inputCode code entered by user
	 * @return this
	 * @throws OtpValidationException if validation fails
	 */
	public Totp verify(final String inputCode) throws OtpValidationException {
		return this.verify(inputCode, true);
	}
	
	/**
	 * Verify the given TOTP code <b>without</b> the scratch codes as fallback.
	 * 
	 * It is guaranteed that an {@link OtpValidationException} is thrown by this method if
	 * validation fails.
	 * 
	 * @param inputCode code entered by user
	 * @return this
	 * @throws OtpValidationException if validation fails
	 */
	public Totp verifySecretOnly(final String inputCode) throws OtpValidationException {
		return this.verify(inputCode, false);
	}
	
	/**
	 * Verify the given TOTP code with the scratch codes as fallback.
	 * If any of the scratch codes were used, {@link #scratchCodes()} will return
	 * null in place of it in the array.
	 * Therefore, to make sure scratch codes can only be used once, persist {@link #scratchCodes()}
	 * after calling {@link #verify(String)}.
	 * 
	 * It is guaranteed that an {@link OtpValidationException} is thrown by this method if
	 * validation fails.
	 * 
	 * @param inputCode code entered by user
	 * @param fallbackToScratchCodes if true, also checks whether the inputCode matches one of the scratch codes
	 * @return this
	 * @throws OtpValidationException if validation fails
	 */
	private Totp verify(final String inputCode, final boolean fallbackToScratchCodes) throws OtpValidationException {
		
		if (inputCode == null) {
			throw new OtpValidationException("No TOTP code given.");
		}
		
		try {
			
			final int code = Integer.valueOf(inputCode);
			final long interval = Settings.INTERVAL_S;
			final long time = (long) Math.floor(System.currentTimeMillis() / 1000);
			final long timeOffset = Settings.MAX_TIME_OFFSET_S;
			
			try {
				for (long i = -timeOffset; i <= timeOffset; i += Math.min(timeOffset, interval)) {
					if (this.isValidCode(code, (time + i) / interval)) {
						return this;
					}
				}
			} catch (InvalidKeyException | NoSuchAlgorithmException e) {
				throw new RuntimeException("Could not generate TOTP key.", e);
			}
			
		} catch (NumberFormatException e) {
		}
		
		if (fallbackToScratchCodes && this.isValidScratchCode(inputCode)) {
			return this;
		}
		
		
		throw new OtpValidationException("TOTP code could not be verified.");
		
	}
	
	/**
	 * Check whether the given code is valid at the given time.
	 * @param userCode the otp code entered by the user
	 * @param time unix time divided by interval (seconds-interval)
	 * @return true if the code is valid; false otherwise.
	 * @throws NoSuchAlgorithmException if the {@link Settings#SIGNING_ALG} is unsupported
	 * @throws InvalidKeyException if {@link #secret} is unsuitable for {@value Settings#SIGNING_ALG}
	 */
	private boolean isValidCode(final int userCode, final long time) throws NoSuchAlgorithmException, InvalidKeyException {
		
		final byte[] timeData = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(time).array();
		
		SecretKeySpec signKey = new SecretKeySpec(this.secret, Settings.SIGNING_ALG);
		Mac mac = Mac.getInstance(Settings.SIGNING_ALG);
		mac.init(signKey);
		
		final byte[] hash = mac.doFinal(timeData);
		int offset = hash[hash.length - 1] & 0xF;
		
		byte[] truncatedHash = new byte[Integer.BYTES];
		for (int i = 0; i < truncatedHash.length; i++) {
			truncatedHash[i] = hash[offset + i];
		}
		truncatedHash[0] &= 0b01111111;
		
		int computedCode = ByteBuffer.allocate(Integer.BYTES).put(truncatedHash).getInt(0);
		computedCode %= Math.pow(10, Settings.CODE_LENGTH);
		
		return userCode == computedCode;
		
	}
	
	/**
	 * Check whether the given code matches any of the given scratch codes.
	 * If a scratch code is valid, it is removed from this instances' {@link #scratchCodes} array.
	 * 
	 * @param inputCode otp code entered by the user.
	 * @return true if the scratch code is contained in {@link #scratchCodes}; false otherwise.
	 */
	private boolean isValidScratchCode(final String inputCode) {

		String scratchCode;
		for (int i = 0; i < this.scratchCodes.length; i++) {
			scratchCode = this.scratchCodes[i];
			
			if (scratchCode != null && scratchCode.equals(inputCode)) {
				this.scratchCodes[i] = null;
				return true;
			}
		}
		
		return false;
		
	}
	
	/**
	 * Generate a new random secret and return it.
	 * @return random secret (unencoded)
	 */
	private byte[] generateSecret() {
		
		byte[] secret = new byte[Settings.SECRET_BYTES];
		
		SecureRandom random = new SecureRandom();
		random.nextBytes(secret);
		
		return secret;
		
	}
	
	/**
	 * Generate {@value Settings#SCRATCH_CODE_AMOUNT} random scratch codes
	 * and return them.
	 * @return scratch codes
	 */
	private String[] generateScratchCodes() {
		
		SecureRandom random = new SecureRandom();
		
		String[] scratchCodes = new String[Settings.SCRATCH_CODE_AMOUNT];
		for (int i = 0; i < scratchCodes.length; i++) {
			byte[] randomScratchCode = new byte[Settings.SCRATCH_CODE_LENGTH / 2];
			random.nextBytes(randomScratchCode);
			scratchCodes[i] = Hex.encodeHexString(randomScratchCode);
		}
		
		return scratchCodes;
		
	}
	
	private static class Settings {
		
		/**
		 * This is the signing algorithm the OTP-apps use.
		 */
		private static final String SIGNING_ALG = "HmacSha1";
		
		/**
		 * The TOTP password interval in seconds.
		 * After each interval, the code changes.
		 */
		private static final long INTERVAL_S = 30;
		
		/**
		 * Time offset in seconds in which the old and new
		 * codes are valid.
		 */
		private static final long MAX_TIME_OFFSET_S = 30;
		
		/**
		 * Length in digits of the TOTP code.
		 */
		private static final long CODE_LENGTH = 6;
		
		/**
		 * Specify the length of the secret in bytes.
		 * Note: 	more than 10 bytes do not make a difference, because we only
		 * 			use the last 4 bits as offset (so max. 15 offset).
		 */
		private static final int SECRET_BYTES = 10;
		
		/**
		 * How many scratch codes to generate.
		 */
		private static final int SCRATCH_CODE_AMOUNT = 3;
		
		/**
		 * How many digits a scratch code should have.
		 * Must be an even number.
		 */
		private static final int SCRATCH_CODE_LENGTH = 10;
		
	}

}
