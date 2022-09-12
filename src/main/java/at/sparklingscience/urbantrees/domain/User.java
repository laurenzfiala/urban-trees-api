package at.sparklingscience.urbantrees.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;
import at.sparklingscience.urbantrees.domain.validator.annotation.DateRange;

/**
 * A user for authentication.
 * 
 * @author Laurenz Fiala
 * @since 2018/06/09
 */
public class User extends UserIdentity implements Serializable {

	private static final long serialVersionUID = 20190808L;

	/**
	 * Users' password to authenticate.
	 */
	private String password;
	
	/**
	 * Whether the users' account is currently active or inactive.
	 */
	@NotNull(groups = {ValidationGroups.Read.class})
	private boolean isActive;
	
	/**
	 * Whether the users' credentials are to be replaced or if they are fine.
	 */
	@NotNull(groups = {ValidationGroups.Read.class})
	private boolean isCredentialsNonExpired;
	
	/**
	 * Amount of failed login attempts for this user.
	 */
	@NotNull(groups = {ValidationGroups.Read.class})
	private int failedloginAttempts;
	
	/**
	 * Date of users' last login attempt.
	 */
	@NotNull(groups = {ValidationGroups.Read.class})
	private Date lastLoginAttemptDate;
	
	/**
	 * Date of users' last login.
	 */
	@NotNull(groups = {ValidationGroups.Read.class})
	private Date lastLoginDate;

	/**
	 * Null if no secure link has been generated,
	 * or the secure login key (randomly generated).
	 */
	private String secureLoginKey;

	/**
	 * PIN the user must enter when using their login key.
	 * If this is set, this prevents the login key from being deleted.
	 */
	private String secureLoginKeyPin;
	
	/**
	 * Whether the user is currently using an OTP.
	 */
	private boolean isUsingOtp;
	
	/**
	 * All roles assigned to this user.
	 */
	@NotNull(groups = {ValidationGroups.Read.class})
	private List<Role> roles;
	
	/**
	 * Date at which this user was created.
	 */
	@NotNull
	@DateRange(DateRange.Range.PAST_AND_PRESENT)
	private Date creationDate;
	
	@Override
	public String toString() {
		return "[userId: " + this.getId() + "]";
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isCredentialsNonExpired() {
		return isCredentialsNonExpired;
	}

	public void setCredentialsNonExpired(boolean isCredentialsNonExpired) {
		this.isCredentialsNonExpired = isCredentialsNonExpired;
	}
	
	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public int getFailedloginAttempts() {
		return failedloginAttempts;
	}

	public void setFailedloginAttempts(int failedloginAttempts) {
		this.failedloginAttempts = failedloginAttempts;
	}

	public Date getLastLoginAttemptDate() {
		return lastLoginAttemptDate;
	}

	public void setLastLoginAttemptDate(Date lastLoginAttemptDate) {
		this.lastLoginAttemptDate = lastLoginAttemptDate;
	}

	public String getSecureLoginKey() {
		return secureLoginKey;
	}

	public void setSecureLoginKey(String secureLoginKey) {
		this.secureLoginKey = secureLoginKey;
	}

	public String getSecureLoginKeyPin() {
		return secureLoginKeyPin;
	}

	public void setSecureLoginKeyPin(String secureLoginKeyPin) {
		this.secureLoginKeyPin = secureLoginKeyPin;
	}

	public boolean isUsingOtp() {
		return isUsingOtp;
	}

	public void setUsingOtp(boolean isUsingOtp) {
		this.isUsingOtp = isUsingOtp;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

}
