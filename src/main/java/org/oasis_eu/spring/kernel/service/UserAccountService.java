package org.oasis_eu.spring.kernel.service;

import org.oasis_eu.spring.kernel.model.UserAccount;
import org.oasis_eu.spring.kernel.model.UserInfo;

/**
 * User: jdenanot
 * Date: 8/29/14
 */
public interface UserAccountService {

	public abstract void saveUserAccount(UserAccount userAccount);
	
}
