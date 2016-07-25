package org.oasis_eu.spring.kernel.security;

import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * User: schambon
 * Date: 1/30/14
 */
public class StaticOpenIdCConfiguration implements OpenIdCConfiguration {

    @Value("${kernel.auth.issuer:}")
    protected String issuer;
    @Value("${kernel.auth.auth_endpoint:}")
    protected String authEndpoint;
    @Value("${kernel.auth.token_endpoint:}")
    protected String tokenEndpoint;
    @Value("${kernel.auth.keys_endpoint:}")
    protected String keysEndpoint;
    @Value("${kernel.auth.revoke_endpoint:}")
    protected String revocationEndpoint;
    @Value("${kernel.auth.userinfo_endpoint:}")
    protected String userInfoEndpoint;
    @Value("${kernel.auth.profile_endpoint:}")
    protected String profileEndpoint;
    @Value("${kernel.auth.callback_uri:}")
    protected String callbackUri;
    @Value("${kernel.client_id:}")
    protected String clientId;
    @Value("${kernel.client_secret:}")
    protected String clientSecret;
    @Value("${kernel.scopes_to_require:}")
    protected String scopesToRequire;

    @Value("${kernel.auth.home_uri:}")
    protected String homeUri;

    private boolean mocked;
    private String mockLoginPageUri;
    private String mockProfile;

    private List<String> skippedPaths = new ArrayList<>();

    @Override
    public String getIssuer() {
        return issuer;
    }

    @Override
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    @Override
    public String getAuthEndpoint() {
        return authEndpoint;
    }

    @Override
    public void setAuthEndpoint(String authEndpoint) {
        this.authEndpoint = authEndpoint;
    }

    @Override
    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    @Override
    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    @Override
    public String getKeysEndpoint() {
        return keysEndpoint;
    }

    @Override
    public void setKeysEndpoint(String keysEndpoint) {
        this.keysEndpoint = keysEndpoint;
    }

    @Override
    public String getRevocationEndpoint() {
        return revocationEndpoint;
    }

    @Override
    public void setRevocationEndpoint(String revocationEndpoint) {
        this.revocationEndpoint = revocationEndpoint;
    }

    @Override
    public String getUserInfoEndpoint() {
        return userInfoEndpoint;
    }

    @Override
    public void setUserInfoEndpoint(String userInfoEndpoint) {
        this.userInfoEndpoint = userInfoEndpoint;
    }

    @Override
	public String getProfileEndpoint() {
		return this.profileEndpoint;
	}

	@Override
	public void setProfileEndpoint(String profileEndpoint) {
		this.profileEndpoint = profileEndpoint;
	}

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public String getCallbackUri() {
        return callbackUri;
    }

    @Override
    public void setCallbackUri(String callbackUri) {
        this.callbackUri = callbackUri;
    }

    @Override
    public String getScopesToRequire() {
        return scopesToRequire;
    }

    @Override
    public void setScopesToRequire(String scopesToRequire) {
        this.scopesToRequire = scopesToRequire;
    }

    @Override
    public boolean isMocked() {
        return mocked;
    }

    @Override
    public void setMocked(boolean mocked) {
        this.mocked = mocked;
    }

    @Override
    public String getMockLoginPageUri() {
        return mockLoginPageUri;
    }

    @Override
    public void setMockLoginPageUri(String mockLoginPageUri) {
        this.mockLoginPageUri = mockLoginPageUri;
    }

    @Override
    public String getMockProfile() {
        return mockProfile;
    }

    @Override
    public void setMockProfile(String mockProfile) {
        this.mockProfile = mockProfile;
    }

    @Override
    public boolean skipAuthenticationForPath(String url) {
        return skippedPaths.stream().anyMatch(url::startsWith);
    }

    public void addSkippedPaths(List<String> urls) {
        skippedPaths.addAll(urls);
    }

    @Override
    public String getHomeUri() {
        return homeUri;
    }

    public void setHomeUri(String homeUri) {
        this.homeUri = homeUri;
    }
}
