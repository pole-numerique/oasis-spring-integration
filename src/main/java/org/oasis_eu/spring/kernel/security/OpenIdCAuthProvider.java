package org.oasis_eu.spring.kernel.security;

import org.oasis_eu.spring.kernel.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

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
        return OpenIdCAuthentication.class.isAssignableFrom(authentication);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        if (authentication == null || !supports(authentication.getClass())) {
            return null;
        }

        if (isFetchUserInfo()) {
            OpenIdCAuthentication auth = (OpenIdCAuthentication) authentication;


            String accessToken = auth.getAccessToken();

            UserInfo ui = openIdCService.getUserInfo(accessToken);

            if (ui.getOrganizationId() != null) {
                auth.setAgent(true);
                auth.setOrganizationOasisId(ui.getOrganizationId());
                auth.setOrganizationAdmin(ui.isOrganizationAdmin());
            }

            auth.setUserInfo(ui);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Fetched user info: " + auth.getSubject()
                        + " isAgent: " + auth.isAgent()
                        + " of organization: " + auth.getOrganizationOasisId());
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

