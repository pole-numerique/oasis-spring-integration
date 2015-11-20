package org.oasis_eu.spring.kernel.security;

/**
 * User: schambon
 * Date: 6/27/14
 */
public interface OpenIdCConfiguration {
    String getIssuer();

    void setIssuer(String issuer);

    String getAuthEndpoint();

    void setAuthEndpoint(String authEndpoint);

    String getTokenEndpoint();

    void setTokenEndpoint(String tokenEndpoint);

    String getKeysEndpoint();

    void setKeysEndpoint(String keysEndpoint);

    String getRevocationEndpoint();

    void setRevocationEndpoint(String revocationEndpoint);

    String getUserInfoEndpoint();

    void setUserInfoEndpoint(String userInfoEndpoint);

    String getProfileEndpoint();

    void setProfileEndpoint(String profileEndpoint);

    String getApplicationId();

    void setApplicationId(String applicationId);

    String getClientId();

    void setClientId(String clientId);

    String getClientSecret();

    void setClientSecret(String clientSecret);

    String getCallbackUri();

    void setCallbackUri(String callbackUri);

    String getScopesToRequire();

    void setScopesToRequire(String scopesToRequire);

    boolean isMocked();

    void setMocked(boolean mocked);

    String getMockLoginPageUri();

    void setMockLoginPageUri(String mockLoginPageUri);

    String getMockProfile();

    void setMockProfile(String mockProfile);

    default boolean skipAuthenticationForPath(String path) {
        return false;
    }

    default boolean requireAuthenticationForPath(String path) {
        return false;
    }

    default String getHomeUri() {
        return null;
    }


}
