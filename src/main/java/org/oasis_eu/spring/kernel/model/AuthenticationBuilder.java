package org.oasis_eu.spring.kernel.model;

import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * User: schambon
 * Date: 9/9/14
 */
public class AuthenticationBuilder {

    public static Authentication user() {
        return new UserAuthentication(((OpenIdCAuthentication)SecurityContextHolder.getContext().getAuthentication()).getAccessToken());
    }

    public static Authentication user(String accessToken) {
        return new UserAuthentication(accessToken);
    }

    public static Authentication client(String clientId, String clientSecret) {
        return new ClientAuthentication(clientId, clientSecret);
    }

    public static Authentication none() {
        return new PublicAuthentication();
    }
}
