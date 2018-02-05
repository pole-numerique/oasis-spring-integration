package org.oasis_eu.spring.kernel.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.oasis_eu.spring.kernel.exception.AuthenticationRequiredException;
import org.oasis_eu.spring.kernel.exception.EntityNotFoundException;
import org.oasis_eu.spring.kernel.exception.ForbiddenException;
import org.oasis_eu.spring.kernel.exception.TechnicalErrorException;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.model.Authentication;
import org.oasis_eu.spring.kernel.rest.ResponseProviderInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * User: schambon
 * Date: 9/9/14
 * 
 * Configured in KernelConfiguration with KernelErrorHandler...
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
     * @throws AuthenticationRequiredException if invalid token (i.e. error 401 from Kernel, ex. expired),
     * HttpServerErrorException if error 500 from Kernel (according to KernelErrorHandler)
     */
    public <REQ, RES> ResponseEntity<RES> exchange(String endpoint, HttpMethod method, HttpEntity<REQ> request, 
            Class<RES> responseClass, Authentication auth, Object... pathVariables)
            throws AuthenticationRequiredException, WrongQueryException, TechnicalErrorException {

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
        ResponseEntity<RES> entity = null;
        try{
        	entity = kernelRestTemplate.exchange(endpoint, method, request, responseClass, pathVariables);
        } catch (HttpClientErrorException rceex) {
        	if (rceex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                // thrown by KernelResponseErrorHandler
        		logger.error("Cannot call kernel endpoint {}, invalid token (401 Unauthorized, ex. expired token)", endpoint);
        		throw new AuthenticationRequiredException();
        	}
        	throw rceex; // should not happen according to KernelResponseErrorHandler
        } catch (HttpServerErrorException hseex) {
            // thrown by KernelResponseErrorHandler
            logger.error("Unexpected error : " + hseex.getMessage());
            throw new TechnicalErrorException();
        }

        if (!entity.getStatusCode().is2xxSuccessful()) {
            this.somethingWentWrong(entity); // #166 set message useful to client
        }

        return entity;
    }

    /**
     * Same as exchange() then getBodyOrNull()
     * @param endpoint
     * @param responseClass
     * @param auth
     * @param id
     * @return
     */
    public <T> T getEntityOrNull(String endpoint, Class<T> responseClass, Authentication auth, Object... idOrOtherUriParameters) {
        try {
            ResponseEntity<T> response = exchange(endpoint, HttpMethod.GET, null, responseClass, auth, idOrOtherUriParameters);
            return getBodyOrNull(response, responseClass, endpoint, idOrOtherUriParameters);
        } catch (HttpClientErrorException hcee) {
            return null;
        }
    }
    
    /**
     * Same as exchange() then getBodyUnlessClientError()
     * @param endpoint
     * @param responseClass
     * @param auth
     * @param id
     * @return
     */
    public <T> T getEntityOrException(String endpoint, Class<T> responseClass, Authentication auth, Object... idOrOtherUriParameters)
    		throws EntityNotFoundException, ForbiddenException, WrongQueryException {
        ResponseEntity<T> response = exchange(endpoint, HttpMethod.GET, null, responseClass, auth, idOrOtherUriParameters);
        return getBodyUnlessClientError(response, responseClass, endpoint, idOrOtherUriParameters);
    }
    
    /**
     * If client error, returns null and if not 403 flags SomethingWentWrongInterceptor.
     * Typically used when GETting an entity that may be skipped because is within a list
     * (ex. notifs).
     * Note : when GETting a Kernel Entity, business services
     * - either call getEntityOrNull() (which calls this method)
     * - or themselves call this method after getForEntity() (or possible a skippable PUT / POST / DELETE)
     * - or / and themselves check the response status like in this method in order to ex. throw
     * a more specific error message.
     * @param response with 200 OK or 4xx status
     * @param logger
     * @param entityClazz
     * @param id
     * @param endpoint 
     * @throws HttpClientErrorException
     */
    public <T> T getBodyOrNull(ResponseEntity<T> response,
            Class<T> entityClazz, String endpoint, Object... idOrOtherUriParameters) {
        
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }

        if (response.getStatusCode() == HttpStatus.FORBIDDEN) { // error cases that are OK from a business point of view
            logger.debug("Forbidden " + entityClazz.getSimpleName() + " {} through endpoint {} : error {}",
                    Arrays.asList(idOrOtherUriParameters), endpoint, response.getStatusCode());
            return null; // ex. 403 service.visible:false, see #179 Bug with notifications referring destroyed app instances

        } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) { 
            logger.debug("Cannot find " + entityClazz.getSimpleName() + " {} through endpoint {} : error {}",
                    Arrays.asList(idOrOtherUriParameters), endpoint, response.getStatusCode());
            return null; // ex. 404 - Deleted app (or service ?) instance, see #179 Bug with notifications referring destroyed app instances
            //See #179 Bug with notifications referring destroyed app instances or not others besides 404 ??

        } else if (response.getStatusCode() == HttpStatus.CONFLICT) { // POST / PUT only
            logger.error("Conflict " + entityClazz.getSimpleName() + " {} calling endpoint {} : error {}",
                    Arrays.asList(idOrOtherUriParameters), endpoint, response.getStatusCode());
            return null; // TODO ; user message : "Data conflict, try to refresh the page"
            // (alt : "Somebody tried to update it at the same time as you", "Can't resend invitation")

        }
        // else other client errors :HttpStatus.NOT_FOUND error cases that should be notified "wrong" but not block

        logger.warn("Cannot find " + entityClazz.getSimpleName() + " {} through endpoint {} : error {}",
                Arrays.asList(idOrOtherUriParameters), endpoint, response.getStatusCode());
        return null;
    }
    /**
     * If client error, explodes and flags SomethingWentWrongInterceptor ;
     * typically to be used on end user action ex. POST/PUT, DELETE.
     * @param response
     * @param logger
     * @param entityClazz
     * @param id
     * @param endpoint
     * @return
     * @throws WrongQueryException if any client error
     */
    public <T> T getBodyUnlessClientError(ResponseEntity<T> response,
            Class<T> entityClazz, String endpoint, Object... idOrOtherUriParameters)
            		throws EntityNotFoundException, ForbiddenException, WrongQueryException {

        if (response.getStatusCode().is2xxSuccessful()) {
            //this.somethingWentWrong(response); // TODO rm to test #166, remove it afterwards
            return response.getBody();
        }

        if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
            logger.debug("Forbidden " + entityClazz.getSimpleName() + " {} through endpoint {} : error {}",
                    Arrays.asList(idOrOtherUriParameters), endpoint, response.getStatusCode());
            throw new ForbiddenException(HttpStatus.FORBIDDEN.value()); // user message : "Action forbidden, do you (still) have rights ?"

        } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            logger.error("Cannot find " + entityClazz.getSimpleName() + " {} through endpoint {} : error {}",
                    Arrays.asList(idOrOtherUriParameters), endpoint, response.getStatusCode());
            throw new EntityNotFoundException(HttpStatus.NOT_FOUND.value()); // user message : "Business object not found, possibly hidden or deleted"

        } else if (response.getStatusCode() == HttpStatus.CONFLICT) { // POST / PUT only
            logger.error("Conflict " + entityClazz.getSimpleName() + " {} calling endpoint {} : error {}",
                    Arrays.asList(idOrOtherUriParameters), endpoint, response.getStatusCode());
            throw new WrongQueryException(HttpStatus.CONFLICT.value()); // TODO ; user message : "Data conflict, try to refresh the page"
            // (alt : "Somebody tried to update it at the same time as you", "Can't resend invitation")

        }
        // else other 4xx client errors :
        //throw new HttpClientErrorException(response.getStatusCode(), null, response.getHeaders(), null, null);
        throw new WrongQueryException(response.getStatusCode().value());
    }

    private String toString(Object body) {
        if (body instanceof String) {
            return (String)body;
        }
        if (body instanceof InputStream) {
            try {
                return StreamUtils.copyToString((InputStream) body, Charset.forName("UTF-8"));
            } catch (IOException e) {
                logger.error("Error reading error response body");
            }
        }
        logger.error("Error response body is not a String nor an InputStream : " + body );
        return null;
	}

	public static final String RESPONSE_HEADER_NAME = "X-Oasis-Portal-Kernel-SomethingWentWrong";

    /**
     * #166 Flags the current (ajax) request as gone "wrong" at Kernel level and provides its response.
     * Call it depending on Kernel response status, if possible in Kernel methods.
     * @param kernelResponse
     */
    public void somethingWentWrong(ResponseEntity<?> kernelResponse) {
        String kernelErrorMessage = toString( kernelResponse.getBody() );
        if (kernelErrorMessage == null) {
            kernelErrorMessage = "-"; // If the header content is empty, header is not going to be set in the response
        }
        HttpServletResponse appResponse = ResponseProviderInterceptor.getResponse();
        if (appResponse != null) {
            appResponse.setHeader(RESPONSE_HEADER_NAME, kernelErrorMessage);
        } // else ex. mock tests
    }
    
}
