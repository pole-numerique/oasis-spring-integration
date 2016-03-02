package org.oasis_eu.spring.kernel.model;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

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
        return "Basic " + BaseEncoding.base64().encode(
            String.format(Locale.ROOT, "%s:%s", clientId, clientSecret).getBytes(StandardCharsets.UTF_8));
    }


    public ClientAuthentication(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
}
