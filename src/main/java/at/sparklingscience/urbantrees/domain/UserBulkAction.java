package at.sparklingscience.urbantrees.domain;

import at.sparklingscience.urbantrees.controller.AdminController;

/**
 * Holds all possible bulk actions supported by
 * the {@link AdminController} for users.
 * 
 * @author Laurenz Fiala
 * @since 2021/02/19
 */
public enum UserBulkAction {
	
	EXPIRE_CREDENTIALS,
	CREATE_LOGIN_LINKS,
	ADD_ROLES,
	REMOVE_ROLES,
	ACTIVATE,
	INACTIVATE,
	DELETE

}
