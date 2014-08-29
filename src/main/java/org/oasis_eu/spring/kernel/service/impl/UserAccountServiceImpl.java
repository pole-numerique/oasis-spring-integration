package org.oasis_eu.spring.kernel.service.impl;

import org.oasis_eu.spring.kernel.model.UserAccount;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.oasis_eu.spring.kernel.security.OpenIdCService;
import org.oasis_eu.spring.kernel.service.UserAccountService;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * User: jdenanot
 * Date: 8/29/14
 */
@Service
public class UserAccountServiceImpl implements UserAccountService {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(UserAccountServiceImpl.class);

    @Autowired
    private OpenIdCService openIdCService;
    
    /* (non-Javadoc)
	 * @see org.oasis_eu.spring.kernel.service.impl.UserInfoService#saveUserInfo(java.util.Map, java.lang.String)
	 */
    @Override
	public void saveUserAccount(UserAccount userAccount) {
    	OpenIdCAuthentication authentication = getOpenIdCAuthentication();
        if (authentication != null) {
            openIdCService.saveUserAccount(authentication.getAccessToken(), userAccount);
            refreshCurrentUser();
        }
    	
    }
    
    private void refreshCurrentUser() {
    	OpenIdCAuthentication authentication = getOpenIdCAuthentication();
        if (authentication != null) {
            UserInfo userInfo = openIdCService.getUserInfo(authentication.getAccessToken());
            authentication.setUserInfo(userInfo);
        }
    }
    
    private OpenIdCAuthentication getOpenIdCAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OpenIdCAuthentication) {
            return (OpenIdCAuthentication) authentication;
        } else {
            return null;
        }
    }
}
