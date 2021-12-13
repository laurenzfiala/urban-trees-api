package at.sparklingscience.urbantrees.domain;

/**
 * All states a user content entry can be in.
 * 
 * @author Laurenz Fiala
 * @since 2021/07/31
 */
public enum UserContentStatus {

	/**
	 * The user content may only be seen/edited by the user who created
	 * the draft. Admins/approvers may also not view this content.
	 */
	DRAFT,
	
	/**
	 * The user content was published by the creator and awaits approval
	 * by an approver for this content id.
	 */
	AWAITING_APPROVAL,
	
	/**
	 * The user content was approved by an approver and may be viewed by
	 * users with viewing permissions.
	 */
	APPROVED,
	
	/**
	 * An editing user wants to delete a user content, but it is not yet
	 * deleted by an approver. The content was previously approved.
	 */
	APPROVED_AWAITING_DELETION,
	
	/**
	 * An editing user wants to delete a user content, but it is not yet
	 * deleted by an approver. The content was previously NOT approved.
	 */
	UNAPPROVED_AWAITING_DELETION,
	
	/**
	 * The user content is deleted and is not depended on by other current
	 * user content.
	 */
	DELETED;
	
}
