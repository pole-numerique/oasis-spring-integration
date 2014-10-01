package org.oasis_eu.spring.kernel.service;

import org.oasis_eu.spring.kernel.exception.AuthenticationRequiredException;
import org.oasis_eu.spring.kernel.model.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * User: schambon
 * Date: 9/9/14
 */
@Service
public class Kernel {

    private static final Logger logger = LoggerFactory.getLogger(Kernel.class);

    @Autowired
    private RestTemplate kernelRestTemplate;


    public <REQ, RES> ResponseEntity<RES> exchange(String endpoint, HttpMethod method, HttpEntity<REQ> request, Class<RES> responseClass, Authentication auth, Object... pathVariables) {

        if (auth != null && auth.hasAuthenticationHeader()) {
            if (request == null) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", auth.getAuthenticationHeader());

                request = new HttpEntity<>(headers);
            } else {
                HttpHeaders headers = request.getHeaders();
                HttpHeaders newHeaders = new HttpHeaders();

                for(Map.Entry<String, List<String>> entry : headers.entrySet()) {
                    newHeaders.put(entry.getKey(), entry.getValue());
                }

                newHeaders.add("Authorization", auth.getAuthenticationHeader());

                request = new HttpEntity<>(request.getBody(), newHeaders);
            }
        }

        ResponseEntity<RES> entity = kernelRestTemplate.exchange(endpoint, method, request, responseClass, pathVariables);
        if (entity.getStatusCode().value() == 401) {
            logger.error("Cannot call kernel endpoint {}, invalid token", endpoint);
            throw new AuthenticationRequiredException();
        }
        return entity;
    }

    public <T> T getForObject(String endpoint, Class<T> responseClass, Authentication auth, Object... uriParameters) {
        return exchange(endpoint, HttpMethod.GET, null, responseClass, auth, uriParameters).getBody();
    }

    public <T> ResponseEntity<T> getForEntity(String endpoint, Class<T> responseClass, Authentication auth, Object... uriParameters) {
        return exchange(endpoint, HttpMethod.GET, null, responseClass, auth, uriParameters);
    }
}
