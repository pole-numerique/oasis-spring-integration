package org.oasis_eu.spring.kernel.model;

import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * User: schambon
 * Date: 9/9/14
 */
public class AuthenticationBuilder {

    public static Authentication user() {
        OpenIdCAuthentication openIdCAuth = ((OpenIdCAuthentication)SecurityContextHolder.getContext().getAuthentication());
        if (openIdCAuth == null) {
            return null; // happens on notifications REST call, when just logged out from notifications page
        }
        return new UserAuthentication(openIdCAuth.getAccessToken());
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

    public static Authentication userIfExists() {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof OpenIdCAuthentication) {
            return user();
        } else return none();
    }
}
