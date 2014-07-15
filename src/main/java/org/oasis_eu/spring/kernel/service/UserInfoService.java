package org.oasis_eu.spring.kernel.service;

import java.io.Serializable;
import java.util.Map;

import org.oasis_eu.spring.kernel.model.UserInfo;

/**
 * User: schambon
 * Date: 6/13/14
 */
public interface UserInfoService {

	public abstract UserInfo currentUser();

	public abstract void saveUserInfo(Map<String, Serializable> userProperties,
			String claim);
	
}
