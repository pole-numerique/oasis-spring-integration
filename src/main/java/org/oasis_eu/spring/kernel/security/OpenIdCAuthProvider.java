package org.oasis_eu.spring.kernel.security;

import org.oasis_eu.spring.kernel.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * User: schambon
 * Date: 1/30/14
 */
public class OpenIdCAuthProvider implements AuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdCAuthProvider.class);

    private boolean fetchUserInfo;

    @Autowired
    private OpenIdCService openIdCService;

    @Override
    public boolean supports(Class<?> authentication) {
        return OpenIdCAuthentication.class.isAssignableFrom(authentication) || PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    @SuppressWarnings("deprecation")
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        if (authentication == null || !supports(authentication.getClass())) {
            return null;
        }

        if (isFetchUserInfo()) {
            OpenIdCAuthentication auth = (OpenIdCAuthentication) authentication;
            
            auth.setUserInfo(openIdCService.getUserInfo(auth));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Fetched user info: {}", auth.getSubject());
            }
        } else {
            LOGGER.debug("User info fetching disabled in configuration");
        }

        authentication.setAuthenticated(true);

        return authentication;
    }

    public void setFetchUserInfo(boolean fetchUserInfo) {
        this.fetchUserInfo = fetchUserInfo;
    }

    public boolean isFetchUserInfo() {
        return fetchUserInfo;
    }

}

