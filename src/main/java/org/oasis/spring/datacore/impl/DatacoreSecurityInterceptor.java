package org.oasis.spring.datacore.impl;

import org.apache.commons.codec.binary.Base64;
import org.oasis.spring.kernel.security.OpenIdCAuthentication;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

/**
 * User: schambon
 * Date: 1/14/14
 */
public class DatacoreSecurityInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication instanceof OpenIdCAuthentication) {
            request.getHeaders().add("Authorization", "Bearer " + ((OpenIdCAuthentication) authentication).getAccessToken());
        } else {
            // in debug/dev mode...
            request.getHeaders().add("Authorization", "Basic " + new String(Base64.encodeBase64("admin:admin".getBytes())));
        }
        return execution.execute(request, body);
    }
}
