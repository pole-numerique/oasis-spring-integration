package org.oasis_eu.spring.kernel.security;

import org.oasis_eu.spring.kernel.model.UserInfo;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;

/**
 * User: schambon
 * Date: 1/30/14
 */
public class OpenIdCAuthentication extends AbstractAuthenticationToken {
    private static final long serialVersionUID = -2562869709198729870L;

    private static Collection<? extends GrantedAuthority> authorities = Collections.emptyList();

    private String accessToken;
    private String idToken;
    private String subject;
    private Instant accessTokenIssuedAt;
    private Instant accessTokenExpires;
    private boolean appUser;
    private boolean appAdmin;
    private UserInfo userInfo;
    private String refreshToken;
    private String refreshNonce;
    private String acr;

    public OpenIdCAuthentication(String subject, String accessToken, String idToken, Instant iat, Instant exp, boolean appUser, boolean appAdmin) {
        super(authorities);

        this.subject = subject;
        this.accessToken = accessToken;
        this.idToken = idToken;
        this.accessTokenIssuedAt = iat;
        this.accessTokenExpires = exp;
        this.appUser = appUser;
        this.appAdmin = appAdmin;
    }

    @Override
    public Object getCredentials() {
        return accessToken;
    }

    @Override
    public Object getPrincipal() {
        return subject;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getSubject() {
        return subject;
    }

    public Instant getAccessTokenIssuedAt() {
        return accessTokenIssuedAt;
    }

    public Instant getAccessTokenExpires() {
        return accessTokenExpires;
    }

    public boolean isAppUser() {
        return appUser;
    }

    public boolean isAppAdmin() {
        return appAdmin;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAcr() {
        return acr;
    }

    public void setAcr(String acr) {
        this.acr = acr;
    }

    @Override
    public String toString() {
        return "OpenIdCAuthentication{\n" +
                "  subject='" + subject + "\'\n" +
                "  accessTokenIssuedAt=" + accessTokenIssuedAt + "\n" +
                "  accessTokenExpires=" + accessTokenExpires + "\n" +
                "  appUser=" + appUser + "\n" +
                "  appAdmin=" + appAdmin + "\n" +
                "  acr=" + acr + "\n" +
                '}';
    }

    public void setRefreshNonce(String refreshNonce) {
        this.refreshNonce = refreshNonce;
    }

    public String getRefreshNonce() {
        return refreshNonce;
    }
}
