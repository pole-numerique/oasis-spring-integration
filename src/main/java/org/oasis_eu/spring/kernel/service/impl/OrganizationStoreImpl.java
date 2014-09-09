package org.oasis_eu.spring.kernel.service.impl;

import com.nimbusds.jose.util.Base64;
import org.oasis_eu.spring.kernel.service.Kernel;
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

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;

/**
 * User: schambon
 * Date: 6/25/14
 */
@Service
public class OrganizationStoreImpl implements OrganizationStore {

    @Autowired
    private Kernel kernel;

    @Autowired
    private OpenIdCConfiguration configuration;

    @Value("${kernel.user_directory_endpoint}")
    private String endpoint;


    @Override
    public Organization find(String organizationId) {
        String uri = UriComponentsBuilder.fromUriString(endpoint)
                .path("/org/{organizationId}")
                .buildAndExpand(organizationId)
                .encode()
                .toUriString();

        return kernel.exchange(uri, HttpMethod.GET, null, Organization.class, user()).getBody();

    }
}
