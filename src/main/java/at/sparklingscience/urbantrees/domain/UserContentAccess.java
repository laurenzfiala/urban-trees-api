package at.sparklingscience.urbantrees.domain;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * One access configuration defined in user_data.content_access.
 * @author Laurenz Fiala
 * @since 2021/08/06
 */
public class UserContentAccess {

	@Min(1)
	private long id;
	
	@NotNull
	private String contentPath;
	
	private boolean enabled;
	
	private boolean keepHistory;
	
	private String description;
	
	private boolean anonAllowView;
	
	private boolean anonAllowEdit;
	
	private Role anonApprovalByRole;
	
	private boolean userAllowView;
	
	private boolean userAllowEdit;
	
	private Role userApprovalByRole;
	
	private List<UserContentAccessRole> roleAccess;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getContentPath() {
		return contentPath;
	}

	public void setContentPath(String contentPath) {
		this.contentPath = contentPath;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isAnonAllowView() {
		return anonAllowView;
	}

	public void setAnonAllowView(boolean anonAllowView) {
		this.anonAllowView = anonAllowView;
	}

	public boolean isAnonAllowEdit() {
		return anonAllowEdit;
	}

	public void setAnonAllowEdit(boolean anonAllowEdit) {
		this.anonAllowEdit = anonAllowEdit;
	}

	public Role getAnonApprovalByRole() {
		return anonApprovalByRole;
	}

	public void setAnonApprovalByRole(Role anonApprovalByRole) {
		this.anonApprovalByRole = anonApprovalByRole;
	}

	public boolean isUserAllowView() {
		return userAllowView;
	}

	public void setUserAllowView(boolean userAllowView) {
		this.userAllowView = userAllowView;
	}

	public boolean isUserAllowEdit() {
		return userAllowEdit;
	}

	public void setUserAllowEdit(boolean userAllowEdit) {
		this.userAllowEdit = userAllowEdit;
	}

	public Role getUserApprovalByRole() {
		return userApprovalByRole;
	}

	public void setUserApprovalByRole(Role userApprovalByRole) {
		this.userApprovalByRole = userApprovalByRole;
	}

	public List<UserContentAccessRole> getRoleAccess() {
		return roleAccess;
	}

	public void setRoleAccess(List<UserContentAccessRole> roleAccess) {
		this.roleAccess = roleAccess;
	}

	public boolean isKeepHistory() {
		return keepHistory;
	}

	public void setKeepHistory(boolean keepHistory) {
		this.keepHistory = keepHistory;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
