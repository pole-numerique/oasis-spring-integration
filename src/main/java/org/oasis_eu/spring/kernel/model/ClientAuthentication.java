package org.oasis_eu.spring.kernel.model;

import com.google.common.io.BaseEncoding;

/**
 * User: schambon
 * Date: 9/9/14
 */
public class ClientAuthentication implements Authentication {

    private String clientId;
    private String clientSecret;

    @Override
    public boolean hasAuthenticationHeader() {
        return true;
    }

    @Override
    public String getAuthenticationHeader() {
        return String.format("Basic %s",
                BaseEncoding.base64().encode(String.format("%s:%s", clientId, clientSecret).getBytes()));
    }


    public ClientAuthentication(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
}
