package at.sparklingscience.urbantrees.domain;

/**
 * One user content access definition for a user role defined in
 * user_data.content_access_role.
 * @author Laurenz Fiala
 * @since 2021/08/06
 */
public class UserContentAccessRole {
	
	private long id;
	
	private long contentAccessId;
	
	private Role role;
	
	private boolean allowView;
	
	private boolean allowEdit;
	
	private Role approvalByRole;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getContentAccessId() {
		return contentAccessId;
	}

	public void setContentAccessId(long contentAccessId) {
		this.contentAccessId = contentAccessId;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public boolean isAllowView() {
		return allowView;
	}

	public void setAllowView(boolean allowView) {
		this.allowView = allowView;
	}

	public boolean isAllowEdit() {
		return allowEdit;
	}

	public void setAllowEdit(boolean allowEdit) {
		this.allowEdit = allowEdit;
	}

	public Role getApprovalByRole() {
		return approvalByRole;
	}

	public void setApprovalByRole(Role approvalByRole) {
		this.approvalByRole = approvalByRole;
	}
	
}
