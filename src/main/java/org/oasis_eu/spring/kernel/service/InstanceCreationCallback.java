package org.oasis_eu.spring.kernel.service;

import org.oasis_eu.spring.kernel.model.instance.InstanceCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.client;

/**
 * Used to notify the Kernel that an instance has been created
 *
 * User: schambon
 * Date: 7/1/14
 */
@Service
public class InstanceCreationCallback {

    private static final Logger logger = LoggerFactory.getLogger(InstanceCreationCallback.class);

    @Autowired
    private Kernel kernel;


    /**
     * Acquit the creation of the application instance, and provide the list of services to create
     *
     * @param endpoint
     * @param instanceCreated
     * @param clientId
     * @param clientSecret
     * @return the service identifiers that have been created
     * @throws org.oasis_eu.spring.kernel.service.InstanceCreationException if for some reason the services could not be created
     */
    public Map<String, String> acquitInstanceCreated(String endpoint, InstanceCreated instanceCreated, String clientId, String clientSecret) {

        logger.info("Acquitting creation of instance with client id: {}\nFull instance is: {}", clientId, instanceCreated);

        ResponseEntity<Map> result = kernel.exchange(endpoint, HttpMethod.POST, new HttpEntity<>(instanceCreated), Map.class, client(clientId, clientSecret));

        if (result.getStatusCode().is2xxSuccessful()) {
            return result.getBody();
        } else {
            throw new InstanceCreationException(result.getStatusCode().getReasonPhrase());
        }
    }


    public void fail(String endpoint, String clientId, String clientSecret) {
        logger.info("Failing instance creation {}", endpoint);

        kernel.exchange(endpoint, HttpMethod.DELETE, null, Void.class, client(clientId, clientSecret));
    }
}
