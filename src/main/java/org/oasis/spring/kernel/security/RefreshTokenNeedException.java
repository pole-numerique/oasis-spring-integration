package org.oasis.spring.kernel.security;

import org.springframework.security.core.AuthenticationException;

/**
 * User: schambon
 * Date: 1/30/14
 */
public class RefreshTokenNeedException extends AuthenticationException {
    public RefreshTokenNeedException(String msg, Throwable t) {
        super(msg, t);
    }

    public RefreshTokenNeedException(String msg) {
        super(msg);
    }
}
