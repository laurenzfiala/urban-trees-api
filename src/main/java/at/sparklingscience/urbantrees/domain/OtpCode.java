package at.sparklingscience.urbantrees.domain;

import javax.validation.constraints.NotNull;

/**
 * A user-entered OTP code.
 * 
 * @author Laurenz Fiala
 * @since 2020/04/23
 */
public class OtpCode {

	@NotNull
	private String code;
	
	public OtpCode() {
	}
	
	public OtpCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	@Override
		public String toString() {
			return "OtpCode[******]";
		}

}
