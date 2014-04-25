package org.oasis_eu.spring.kernel.security;

/**
 * User: schambon
 * Date: 1/30/14
 */
public class OpenIdCConfiguration {

    String issuer;
    String authEndpoint;
    String tokenEndpoint;
    String keysEndpoint;
    String revocationEndpoint;
    String userInfoEndpoint;

    String applicationId;
    String clientId;
    String clientSecret;

    String scopesToRequire;

    String callbackUri;

    boolean mocked;
    String mockLoginPageUri;
    String mockProfile;

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAuthEndpoint() {
        return authEndpoint;
    }

    public void setAuthEndpoint(String authEndpoint) {
        this.authEndpoint = authEndpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getKeysEndpoint() {
        return keysEndpoint;
    }

    public void setKeysEndpoint(String keysEndpoint) {
        this.keysEndpoint = keysEndpoint;
    }

    public String getRevocationEndpoint() {
        return revocationEndpoint;
    }

    public void setRevocationEndpoint(String revocationEndpoint) {
        this.revocationEndpoint = revocationEndpoint;
    }

    public String getUserInfoEndpoint() {
        return userInfoEndpoint;
    }

    public void setUserInfoEndpoint(String userInfoEndpoint) {
        this.userInfoEndpoint = userInfoEndpoint;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getCallbackUri() {
        return callbackUri;
    }

    public void setCallbackUri(String callbackUri) {
        this.callbackUri = callbackUri;
    }

    public String getScopesToRequire() {
        return scopesToRequire;
    }

    public void setScopesToRequire(String scopesToRequire) {
        this.scopesToRequire = scopesToRequire;
    }

    public boolean isMocked() {
        return mocked;
    }

    public void setMocked(boolean mocked) {
        this.mocked = mocked;
    }

    public String getMockLoginPageUri() {
        return mockLoginPageUri;
    }

    public void setMockLoginPageUri(String mockLoginPageUri) {
        this.mockLoginPageUri = mockLoginPageUri;
    }

    public String getMockProfile() {
        return mockProfile;
    }

    public void setMockProfile(String mockProfile) {
        this.mockProfile = mockProfile;
    }

}
