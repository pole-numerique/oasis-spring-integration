package org.oasis_eu.spring.kernel.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.oasis_eu.spring.kernel.exception.AuthenticationRequiredException;
import org.oasis_eu.spring.kernel.exception.ForbiddenException;
import org.oasis_eu.spring.kernel.model.Authentication;
import org.oasis_eu.spring.kernel.rest.ResponseProviderInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * User: schambon
 * Date: 9/9/14
 */
@Service
public class Kernel {

    private static final Logger logger = LoggerFactory.getLogger(Kernel.class);

    @Autowired
    private RestTemplate kernelRestTemplate;

    /**
     * 
     * @param endpoint
     * @param method
     * @param request
     * @param responseClass
     * @param auth
     * @param pathVariables
     * @return entity that may be 404 Not Found (deleted app instance, service ?) or 403 Forbidden (when service.visible:false)
     * see #179 Bug with notifications referring destroyed app instances
     * @throws AuthenticationRequiredException if invalid token (ex. expired)
     */
    public <REQ, RES> ResponseEntity<RES> exchange(String endpoint,
            HttpMethod method, HttpEntity<REQ> request, Class<RES> responseClass, Authentication auth, Object... pathVariables)
            throws AuthenticationRequiredException, ForbiddenException {
        
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
            logger.error("Cannot call kernel endpoint {}, invalid token (401 Unauthorized, ex. expired token)", endpoint);
            throw new AuthenticationRequiredException();
        }
        // NB. let further error handling be done in caller, raising exceptions already there triggers errors like
        // #179 Bug with notifications referring destroyed app instances
        // including whether or not "something went wrong" (#166) because it must be decided by each portal service may
        // (even if usually 403 = nothing wrong and others including 404 = something wrong)
        return entity;
    }

    public <T> T getForObject(String endpoint, Class<T> responseClass, Authentication auth, Object... uriParameters) {
        return exchange(endpoint, HttpMethod.GET, null, responseClass, auth, uriParameters).getBody();
    }

    public <T> ResponseEntity<T> getForEntity(String endpoint, Class<T> responseClass, Authentication auth, Object... uriParameters) {
        return exchange(endpoint, HttpMethod.GET, null, responseClass, auth, uriParameters);
    }

    /**
     * Same as exchange() then handleResourceError()
     * @param endpoint
     * @param responseClass
     * @param auth
     * @param id
     * @return
     */
    public <T> T getEntityOrNull(String endpoint, Class<T> responseClass, Authentication auth, String id) {
        ResponseEntity<T> response = exchange(endpoint, HttpMethod.GET, null, responseClass, auth, id);
        return acceptClientEntityError(response, logger, responseClass, id, endpoint);
    }
    
    /**
     * If client error, returns null and if not 403 flags SomethingWentWrongInterceptor.
     * When GETting a Kernel Entity, either business services call getEntityOrNull()
     * or themselves call this method after getForEntity().
     * NB. useless when getting lists of Entities.
     * TODO move to service ?
     * @param response
     * @param logger
     * @param entityClazz
     * @param id
     * @param endpoint 
     * @throws HttpClientErrorException
     */
    public <T> T acceptClientEntityError(ResponseEntity<T> response,
            Logger logger, Class<T> entityClazz, String id, String endpoint) throws HttpClientErrorException {
        
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
            
        } else if (response.getStatusCode().value() == 403) { // error cases that are OK from a business point of view
            logger.debug("Cannot find " + entityClazz.getSimpleName() + " {} through endpoint {} : error {}", id, endpoint, response.getStatusCode());
            return null; // ex. 403 service.visible:false, see #179 Bug with notifications referring destroyed app instances

        } else if (response.getStatusCode().is4xxClientError()) { // error cases that should be notified but not block
            logger.warn("Cannot find " + entityClazz.getSimpleName() + " {} through endpoint {} : error {}", id, endpoint, response.getStatusCode());
            this.somethingWentWrong();
            return null; // ex. 404 deleted app instance (or service ?), see #179 Bug with notifications referring destroyed app instances
            // or not others besides 404 ??
            
        } else { // server error : abort
            throw new HttpClientErrorException(response.getStatusCode(), response.getStatusCode().getReasonPhrase());
        }
    }

    public static final String RESPONSE_HEADER_NAME = "X-Oasis-Portal-Kernel-SomethingWentWrong";
    public static final String RESPONSE_HEADER_VALUE = "true";
    
    public void somethingWentWrong() {
        HttpServletResponse response = ResponseProviderInterceptor.getResponse();
        if (response != null) {
            response.setHeader(RESPONSE_HEADER_NAME, "true"); //System.out.println("" + java.util.Collections.list(request.getAttributeNames()));   
        } // else ex. mock tests
    }
    
}
