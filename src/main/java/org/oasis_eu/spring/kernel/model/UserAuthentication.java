package org.oasis_eu.spring.kernel.model;

/**
 * User: schambon
 * Date: 9/9/14
 */
public class UserAuthentication implements Authentication {

    private String token;

    @Override
    public boolean hasAuthenticationHeader() {
        return true;
    }

    @Override
    public String getAuthenticationHeader() {
        return "Bearer " + token;
    }

    public UserAuthentication(String token) {
        this.token = token;
    }
}
