package org.oasis_eu.spring.kernel.security;

import org.oasis_eu.spring.kernel.service.Kernel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.client;

/**
 * User: schambon
 * Date: 1/8/14
 */
public class OasisLogoutHandler implements LogoutSuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OasisLogoutHandler.class);

    @Autowired
    private OpenIdCConfiguration configuration;

    @Value("${kernel.auth.logout_endpoint}")
    private String logoutEndpoint;

    @Autowired
    private Kernel kernel;
    
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // Okay, at this stage we have ended the Spring authentication,
        // now we want to cleanly log out of Ozwillo
        if (authentication instanceof OpenIdCAuthentication) {
            OpenIdCAuthentication token = (OpenIdCAuthentication) authentication;

            // 1. Revoke token
            try {
                // Direct Kernel API call kernel.exchange...
                ResponseEntity<String> entity = kernel.exchange(
                        configuration.getRevocationEndpoint(),
                        HttpMethod.POST,
                        this.createRevocationRequest(token),
                        String.class,
                        client(configuration.getClientId(), configuration.getClientSecret()));

                if (entity.getStatusCode().value() != 200) { // RFC 7009 says the response should be 200 unless we provided an unknown token type
                    LOGGER.error("Cannot handle revocation response with code: " + entity.getStatusCode());
                    LOGGER.error("Payload is: " + entity.getBody());
                }
            } catch (Throwable t) {
                // catch-all to be sure we proceed to invalidating the user session.
                LOGGER.error("Cannot revoke access token", t);
            }

            // 2. to be sure we don't leak anything (though Spring should do the job), invalidate the session
            request.getSession().invalidate();

            // 3. redirect to the general Ozwillo logout page
            UriComponentsBuilder logoutBuilder = UriComponentsBuilder.fromHttpUrl(logoutEndpoint)
                    .queryParam("id_token_hint", token.getIdToken());
            String kernelLogout;
            if (configuration.getPostLogoutRedirectUri() != null) {
                kernelLogout = logoutBuilder
                        .queryParam("post_logout_redirect_uri", configuration.getPostLogoutRedirectUri())
                        .build()
                        .toUriString();
            } else {
                kernelLogout = logoutBuilder.build().toUriString();
            }

            response.sendRedirect(kernelLogout);

        } else {
            request.getSession().invalidate();

            String s = authentication != null ? authentication.getClass().toString() : "null";
            LOGGER.error("Authentication token " + s + " is not an OIDCAuthenticationToken; I don't know what to do with it");

            response.sendRedirect(logoutEndpoint);
        }
    }

    private HttpEntity<MultiValueMap<String, String>> createRevocationRequest(OpenIdCAuthentication token){
    	MultiValueMap<String, String> revocationRequest = new LinkedMultiValueMap<>();
        revocationRequest.add("token", token.getAccessToken());
        revocationRequest.add("token_type_hint", "access_token");
        
        // NB. HttpEntity has two constructors accepting one param. If a MultiValueMap is passed, it will 
		// use the one that set the passed value IN the header. Here the Token is required to be set in the body.
		return new HttpEntity<>(revocationRequest, null);
    }
}
