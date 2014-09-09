package org.oasis_eu.spring.kernel.service;

import org.oasis_eu.spring.kernel.model.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * User: schambon
 * Date: 9/9/14
 */
@Service
public class Kernel {

    @Autowired
    private RestTemplate kernelRestTemplate;


    public <REQ, RES> ResponseEntity<RES> exchange(String endpoint, HttpMethod method, HttpEntity<REQ> request, Class<RES> responseClass, Authentication auth, Object... pathVariables) {

        if (auth != null && auth.hasAuthenticationHeader()) {
            if (request == null) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", auth.getAuthenticationHeader());

                request = new HttpEntity<>(headers);
            } else {
                request.getHeaders().add("Authorization", auth.getAuthenticationHeader());
            }
        }

        return kernelRestTemplate.exchange(endpoint, method, request, responseClass, pathVariables);
    }
}
