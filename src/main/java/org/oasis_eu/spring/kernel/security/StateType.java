package org.oasis_eu.spring.kernel.security;

/**
 * User: schambon
 * Date: 8/4/14
 */
public enum StateType {

    SIMPLE_CHECK,                    // this state is a transparent request to check if there's already a session going
    AUTH_REQUEST                     // this state is a full-blown auth request

}
