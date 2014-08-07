package org.oasis_eu.spring.kernel.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Doesn't replace Spring Sec's default ExceptionTranslationFilter, but adds to it
 *
 * User: schambon
 * Date: 1/30/14
 */
public class OasisExceptionTranslationFilter extends GenericFilterBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(OasisExceptionTranslationFilter.class);

    private RequestCache requestCache = new HttpSessionRequestCache();
    private AuthenticationEntryPoint authenticationEntryPoint;


    public OasisExceptionTranslationFilter(AuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        try {
            chain.doFilter(request, response);

        } catch (RefreshTokenNeedException ex) {

            LOGGER.debug("Caught exception: ", ex);

            requestCache.saveRequest(request, response);
            LOGGER.debug("Calling Authentication entry point.");
            authenticationEntryPoint.commence(request, response, ex);

        }
    }


}
