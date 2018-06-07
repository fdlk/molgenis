package org.molgenis.security.account;

import org.molgenis.data.security.auth.User;

public interface AccountService
{
	String ROLE_USER = "USER";

	void createUser(User user, String baseActivationUri)
			throws UsernameAlreadyExistsException, EmailAlreadyExistsException;

	/**
	 * Activate a registered user
	 */
	void activateUser(String activationCode);

	void changePassword(String username, String newPassword);

	void resetPassword(String userEmail);
}