package org.oasis_eu.spring.kernel.service.impl;

import com.nimbusds.jose.util.Base64;
import org.oasis_eu.spring.kernel.service.OrganizationStore;
import org.oasis_eu.spring.kernel.model.Organization;
import org.oasis_eu.spring.kernel.security.OpenIdCConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * User: schambon
 * Date: 6/25/14
 */
@Service
public class OrganizationStoreImpl implements OrganizationStore {

    @Autowired
    private RestTemplate kernelRestTemplate;

    @Autowired
    private OpenIdCConfiguration configuration;

    @Value("${kernel.user_directory_endpoint}")
    private String endpoint;

//    @Override
//    public Organization find(String id) {
//        return kernelRestTemplate.getForObject(endpoint + "/org/{id}", Organization.class, id);
//
//
//    }

    @Override
    public Organization find(String organizationId) {
        String uri = UriComponentsBuilder.fromUriString(endpoint)
                .path("/org/{organizationId}")
                .buildAndExpand(organizationId)
                .encode()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Basic %s", Base64.encode(String.format("%s:%s", configuration.getClientId(), configuration.getClientSecret()))));
        return kernelRestTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), Organization.class).getBody();

    }
}
