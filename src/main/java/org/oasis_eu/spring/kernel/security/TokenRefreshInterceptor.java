package org.oasis_eu.spring.kernel.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;

/**
 * User: schambon
 * Date: 1/30/14
 */
public class TokenRefreshInterceptor extends HandlerInterceptorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(TokenRefreshInterceptor.class);

    // duration in seconds of the "red zone" for token expiry
    // ie we'll preemptively renew the token when we are less than this number of seconds away from actual token expiry
    @Value("${kernel.token.expiry:60}")
    long tokenExpiryThreshold;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (needsTokenRefresh()) throw new RefreshTokenNeedException("Access token is about to expire, needs refresh");

        return true;
    }


    // checks if there is an access token in the security context; if so, is this token about to expire?
    private boolean needsTokenRefresh() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth instanceof OpenIdCAuthentication) {
            OpenIdCAuthentication openAuth = (OpenIdCAuthentication) auth;



            if (openAuth.getAccessTokenExpires().isBefore(Instant.now().plusSeconds(tokenExpiryThreshold))) {
                logger.info("Token about to expire, at {} while now+threshold is {}", openAuth.getAccessTokenExpires(), Instant.now().plusSeconds(tokenExpiryThreshold));

                return true;
            }
        }


        return false;
    }

}
