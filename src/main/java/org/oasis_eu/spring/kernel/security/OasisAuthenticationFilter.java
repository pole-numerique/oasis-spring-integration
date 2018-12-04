package org.oasis_eu.spring.kernel.security;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * User: schambon
 * Date: 8/7/14
 */
public class OasisAuthenticationFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(OasisAuthenticationFilter.class);

    private static final String LOGIN = "/login";
    private static final String CALLBACK = "/callback";

    @Autowired
    private OpenIdCConfiguration configuration;

    @Autowired
    private OpenIdCService openIdCService;

    private AuthenticationManager authenticationManager;

    private RequestCache requestCache = new HttpSessionRequestCache();

    private AuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();

    @Value("${application.security.check_if_external_referrer:false}")
    private boolean checkIfExternalReferrer;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        logger.debug("Filtering request for {}", req.getServletPath());

        boolean onlyIfAuthenticated = configuration.requireAuthenticationForPath(req.getServletPath());
        boolean unauthenticated = configuration.skipAuthenticationForPath(req.getServletPath());

        if (LOGIN.equals(req.getServletPath())) {
            doLogin(req, res);
        } else if (CALLBACK.equals(req.getServletPath())) {
            if (doVerify(req, res)) {
                chain.doFilter(req, res);
            }
        } else if (onlyIfAuthenticated && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("Path {} requires pre-authentication, aborting", req.getServletPath());
            res.setStatus(401);
        } else if ((!unauthenticated) && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (doTransparent(req, res)) {
                chain.doFilter(req, res);
            }
        }else {
            chain.doFilter(request, response);
        }

    }

    private boolean doTransparent(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (checkExternalAuth(req)) {
            logger.debug("Performing transparent auth query");
            requestCache.saveRequest(req, res);

            openIdCService.redirectToAuth(req, res, StateType.SIMPLE_CHECK);
            return false;
        }

        return true;
    }

    private boolean checkExternalAuth(HttpServletRequest req) {
        boolean newSession = req.getSession().isNew();

        if (newSession) {
            return true;
        } else {
            if (checkIfExternalReferrer) {
                // if there is a referrer, and it is not from our application, then let's recheck auth.
                String referrer = req.getHeader("Referer");
                return !Strings.isNullOrEmpty(referrer) && !referrer.startsWith(configuration.getHomeUri()) && req.getParameter("override_referer") == null;
            }
        }

        return false;
    }

    private boolean doVerify(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        logger.debug("Callback - verifying the received parameters");
        String code = request.getParameter("code");
        String error = request.getParameter("error");
        String state = request.getParameter("state");
        if (error != null) {
            logger.error("Got an error from server: {}; request was {}", error, request.getRequestURI());
            // not necessarily a problem: it may mean we are not authenticated
            if (openIdCService.getStateType(state).equals(StateType.SIMPLE_CHECK)) {
                if ("consent_required".equals(error)) {
                    // special case: redirect to the kernel with explicit consent
                    openIdCService.redirectToAuth(request, response, StateType.AUTH_REQUEST);
                    return false;
                }

                SavedRequest savedRequest = requestCache.getRequest(request, response);
                requestCache.removeRequest(request, response);
                String redirectUrl;
                if (savedRequest != null) {
                    redirectUrl = savedRequest.getRedirectUrl();
                } else {
                    String homeUri = configuration.getHomeUri();
                    if (homeUri != null) redirectUrl = homeUri;
                    else redirectUrl = "/";

                }

                response.sendRedirect(UriComponentsBuilder.fromHttpUrl(redirectUrl).queryParam("override_referer", true).build().toString());

            } else {
                requestCache.removeRequest(request, response);
                handle401response(response);
            }
            return false;


        } else if (!Strings.isNullOrEmpty(code) && !Strings.isNullOrEmpty(state)) {
            // we got a code!
            Authentication authentication = processAuthorizationCode(code, state, request);

            if (authentication != null) {
                successfulAuthentication(request, response, authentication);
                return false;
            } else {
                requestCache.removeRequest(request, response);
                handle401response(response);
                return false;
            }
        }

        return true;
    }

    private void handle401response(HttpServletResponse response) throws IOException {

        if (!Strings.isNullOrEmpty(configuration.getError401Uri())) {
            logger.warn("Invalid authentication, redirecting to configured 401 page {}", configuration.getError401Uri());
            response.sendRedirect(configuration.getError401Uri());
        } else {
            logger.warn("Invalid authentication and no configured page. Send a 401 and let the browser manage.");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }

    private void doLogin(HttpServletRequest req, HttpServletResponse res) throws IOException {
        logger.debug("Request for login - redirecting to the kernel");
        openIdCService.redirectToAuth(req, res, StateType.AUTH_REQUEST);
    }

    private Authentication processAuthorizationCode(String code, String state, HttpServletRequest request) {
        HttpSession session = request.getSession();
        String savedState = (String) session.getAttribute(OpenIdCService.STATE);
        String savedNonce = (String) session.getAttribute(OpenIdCService.NONCE);

        session.removeAttribute(OpenIdCService.STATE);
        session.removeAttribute(OpenIdCService.NONCE);

        return openIdCService.processAuthentication(code, state, savedState, savedNonce, configuration.getCallbackUri());

    }

    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            Authentication authResult) throws IOException, ServletException {

        if (logger.isDebugEnabled()) {
            logger.debug("Authentication success. Updating SecurityContextHolder to contain: " + authResult);
        }

        SecurityContextHolder.getContext().setAuthentication(authenticationManager.authenticate(authResult));

        try {
            request.getClass().getMethod("changeSessionId");
            request.changeSessionId();
        } catch (NoSuchMethodException e) {
            logger.debug("server does not support Servlet 3.1");
        }

        successHandler.onAuthenticationSuccess(request, response, authResult);
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setSuccessHandler(AuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }
}
