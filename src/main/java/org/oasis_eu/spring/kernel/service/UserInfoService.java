package org.oasis_eu.spring.kernel.service;

import org.oasis_eu.spring.kernel.model.UserInfo;

/**
 * User: schambon
 * Date: 6/13/14
 */
public interface UserInfoService {

	UserInfo currentUser();
    boolean isAuthenticated();

	
}
