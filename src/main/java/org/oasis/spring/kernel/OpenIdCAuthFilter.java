package org.oasis.spring.kernel;

import java.io.IOException;
import java.security.SecureRandom;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;

/**
 * User: schambon
 * Date: 1/30/14
 */
public class OpenIdCAuthFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdCAuthFilter.class);

    private static final String CALLBACK_URL = "/login";
    private static final String NONCE = "openid_connect_nonce";
    private static final String STATE = "openid_connect_state";

    @Autowired
    private OpenIdCService openIdCService;

    @Autowired
    private OpenIdCConfiguration configuration;

    public OpenIdCAuthFilter() {
        super(CALLBACK_URL);
    }



    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        // three cases:
        // - initial request, i.e. the browser sent a request to /login
        // - refresh token, i.e. we detected that we need to get a new access token
        // - authorization code, i.e. we got back a code!
        // - error, i.e. we got an error back from the server

        String code = request.getParameter("code");
        String error = request.getParameter("error");
        String state = request.getParameter("state");
        if (error != null) {
            LOGGER.debug("Got an error from server");
            // TODO
        } else if (!Strings.isNullOrEmpty(code) && !Strings.isNullOrEmpty(state)) {
            // we got a code!
            Authentication authentication = processAuthorizationCode(code, state, request);

            return authentication;
        } else {
            // send a redirect to auth server
            redirectToAuth(request, response);
        }

        return null;
    }

    private void redirectToAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String state = BaseEncoding.base64Url().encode(bytes);
        random.nextBytes(bytes);
        String nonce = BaseEncoding.base64Url().encode(bytes);

        session.setAttribute(STATE, state);
        session.setAttribute(NONCE, nonce);

        String callbackUri = configuration.getCallbackUri();
        
        String scopesToRequire = configuration.getScopesToRequire();
        String authUri = openIdCService.getAuthUri(state, nonce, callbackUri, scopesToRequire, false);
        response.sendRedirect(authUri);
    }

    private Authentication processAuthorizationCode(String code, String state, HttpServletRequest request) {
        HttpSession session = request.getSession();
        String savedState = (String) session.getAttribute(STATE);
        String savedNonce = (String) session.getAttribute(NONCE);

        session.removeAttribute(STATE);
        session.removeAttribute(NONCE);

        return openIdCService.processAuthentication(code, state, savedState, savedNonce, configuration.getCallbackUri());

    }

}

