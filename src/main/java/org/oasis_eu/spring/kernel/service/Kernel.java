package org.oasis_eu.spring.kernel.service;

import org.oasis_eu.spring.kernel.model.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
                HttpHeaders headers = request.getHeaders();
                HttpHeaders newHeaders = new HttpHeaders();

                for(Map.Entry<String, List<String>> entry : headers.entrySet()) {
                    newHeaders.put(entry.getKey(), entry.getValue());
                }

                newHeaders.add("Authorization", auth.getAuthenticationHeader());

                request = new HttpEntity<>(request.getBody(), newHeaders);
            }
        }

        return kernelRestTemplate.exchange(endpoint, method, request, responseClass, pathVariables);
    }
}
