package org.oasis_eu.spring.kernel.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

/**
 * Responds to queries to /login (only) so should always issue a redirect to kernel/a/auth with a standard state
 * note that the actual authentication processing is up to the preauthenticatedfilter
 *
 * User: schambon
 * Date: 1/30/14
 */
public class OpenIdCAuthFilter extends AbstractAuthenticationProcessingFilter {



    private static final String LOGIN = "/login";

    @Autowired
    private OpenIdCService openIdCService;

    public OpenIdCAuthFilter() {
        super(LOGIN);
    }



    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        logger.info("Attempting authentication (sending redirect to kernel)");

        openIdCService.redirectToAuth(request, response, StateType.AUTH_REQUEST);

        return null;
    }


}

