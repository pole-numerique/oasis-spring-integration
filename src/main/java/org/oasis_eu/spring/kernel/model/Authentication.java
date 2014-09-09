package org.oasis_eu.spring.kernel.model;

/**
 * User: schambon
 * Date: 9/9/14
 */
public interface Authentication {

    String getAuthenticationHeader();
    boolean hasAuthenticationHeader();

}
