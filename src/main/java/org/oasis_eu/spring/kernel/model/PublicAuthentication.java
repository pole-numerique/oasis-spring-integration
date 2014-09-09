package org.oasis_eu.spring.kernel.model;

/**
 * User: schambon
 * Date: 9/9/14
 */
public class PublicAuthentication implements Authentication {

    @Override
    public boolean hasAuthenticationHeader() {
        return false;
    }

    @Override
    public String getAuthenticationHeader() {
        return null;
    }
}
