package at.sparklingscience.urbantrees.domain;

import java.util.List;

/**
 * All states a user content entry can be in.
 * 
 * @author Laurenz Fiala
 * @since 2021/07/31
 */
public enum UserContentStatus {

	// -- Permanent statuses --
	
	/**
	 * The user content may only be seen/edited by the user who created
	 * the draft. Admins/approvers may also not view this content.
	 */
	DRAFT,
	
	/**
	 * The user content was approved by an approver and may be viewed by
	 * users with viewing permissions.
	 */
	APPROVED,
	
	/**
	 * The user content is deleted and is not depended on by other current
	 * user content.
	 */
	DELETED,
	
	// -- Transient statuses (awaiting approval) --
	
	/**
	 * The user content was published by the creator and awaits approval
	 * by an approver for this content id.
	 */
	DRAFT_AWAITING_APPROVAL(DRAFT, APPROVED),
	
	/**
	 * An editing user wants to delete a user content, but it is not yet
	 * deleted by an approver. The content was previously approved.
	 */
	APPROVED_AWAITING_DELETION(APPROVED, DELETED),
	
	/**
	 * An editing user wants to delete a user content, but it is not yet
	 * deleted by an approver. The content was previously awaiting approval.
	 */
	DRAFT_AWAITING_DELETION(DRAFT_AWAITING_APPROVAL, DELETED);
	
	private final UserContentStatus previous, next;
	
	private UserContentStatus() {
		this(null, null);
	}
	
	private UserContentStatus(UserContentStatus previous,
							  UserContentStatus next) {
		this.previous = previous;
		this.next = next;
	}
	
	public UserContentStatus previous() {
		return this.previous;
	}
	
	public boolean hasPrevious() {
		return this.previous != null;
	}
	
	public UserContentStatus next() {
		return this.next;
	}
	
	public boolean hasNext() {
		return this.next != null;
	}
	
	public boolean isPermanent() {
		return !this.hasNext() && !this.hasPrevious();
	}
	
	public boolean isTransient() {
		return !this.isPermanent();
	}
	
	public static List<UserContentStatus> transienT() {
		return List.of(DRAFT_AWAITING_APPROVAL, APPROVED_AWAITING_DELETION, APPROVED_AWAITING_DELETION);
	}
	
	public static List<UserContentStatus> permanent() {
		return List.of(DRAFT, APPROVED, DELETED);
	}
	
}
