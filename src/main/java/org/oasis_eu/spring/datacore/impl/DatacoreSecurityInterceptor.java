package org.oasis_eu.spring.datacore.impl;

import java.io.IOException;

import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * User: schambon
 * Date: 1/14/14
 */

public class DatacoreSecurityInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(DatacoreSecurityInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication instanceof OpenIdCAuthentication) {
            request.getHeaders().add("Authorization", "Bearer " + ((OpenIdCAuthentication) authentication).getAccessToken());
        } else {
            // in debug/dev mode...
            //request.getHeaders().add("Authorization", "Basic " + new String(Base64.encodeBase64("admin:admin".getBytes())));
            /*OpenIdCAuthentication openIdCAuthentication = openIdCService.processAuthentication(null, refreshToken, null, null, refreshTokenNonce, callbackUri);
              request.getHeaders().add("Authorization", "Bearer " + openIdCAuthentication.getAccessToken());*/

            logger.error("No valid authentication is stablished. For non-connection jobs (cron/schedule), "
                    + "try to connect using the admin user with its valid auth token.");
        }
        return execution.execute(request, body);
    }
}
