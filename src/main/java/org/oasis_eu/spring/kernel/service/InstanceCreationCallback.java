package org.oasis_eu.spring.kernel.service;

import org.apache.commons.codec.binary.Base64;
import org.oasis_eu.spring.kernel.model.instance.InstanceCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

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
    private RestTemplate kernelRestTemplate;


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

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "BASIC " + new String(Base64.encodeBase64((clientId + ":" + clientSecret).getBytes())));

        HttpEntity<InstanceCreated> requestEntity = new HttpEntity<>(instanceCreated, headers);

        ResponseEntity<Map> result = kernelRestTemplate.exchange(endpoint, HttpMethod.POST, requestEntity, Map.class);

        if (result.getStatusCode().is2xxSuccessful()) {
            return result.getBody();
        } else {
            throw new InstanceCreationException(result.getStatusCode().getReasonPhrase());
        }
    }


}
