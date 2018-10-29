package at.sparklingscience.urbantrees.domain;

/**
 * Used to change a user's password.
 * 
 * @author Laurenz Fiala
 * @since 2018/0713
 */
public class PasswordReset {

	private String oldPassword;
	private String newPassword;
	
	public String getOldPassword() {
		return oldPassword;
	}
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}
	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
}
