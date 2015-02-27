package org.oasis_eu.spring.kernel.service.impl;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;
import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.userIfExists;

import org.oasis_eu.spring.kernel.model.Organization;
import org.oasis_eu.spring.kernel.service.Kernel;
import org.oasis_eu.spring.kernel.service.OrganizationStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * User: schambon
 * Date: 6/25/14
 */
@Service
public class OrganizationStoreImpl implements OrganizationStore {

    @Autowired
    private Kernel kernel;

    @Value("${kernel.user_directory_endpoint}")
    private String endpoint;


    @Override
    @Cacheable("organizations")
    public Organization find(String organizationId) {
        String uri = UriComponentsBuilder.fromUriString(endpoint)
                .path("/org/{organizationId}")
                .build()
                .toUriString();

        return kernel.getEntityOrNull(uri, Organization.class, userIfExists(), organizationId);
    }

    @Override
    @CachePut(value = "organizations", key = "#result.id")
    public Organization create(Organization organization) {

        String uri = UriComponentsBuilder.fromUriString(endpoint)
                .path("/org")
                .build()
                .toUriString();

        return kernel.exchange(uri, HttpMethod.POST, new HttpEntity<Organization>(organization), Organization.class, user()).getBody();
    }

    @Override
    @CacheEvict(value = "organizations", key = "#organizationId")
    public void delete(String organizationId) {

        String uri = UriComponentsBuilder.fromUriString(endpoint)
                .path("/org/{organizationId}")
                .buildAndExpand(organizationId)
                .encode()
                .toUriString();

        String eTag = kernel.exchange(uri, HttpMethod.GET, null, Organization.class, user()).getHeaders().getETag();

        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", eTag);

        kernel.exchange(uri, HttpMethod.DELETE, new HttpEntity<Object>(headers), Void.class, user());
    }

    @Override
    @CacheEvict(value = "organizations", key = "#org.id")
    public void update(Organization org) {
        String uri = UriComponentsBuilder.fromUriString(endpoint)
                .path("/org/{organizationId}")
                .buildAndExpand(org.getId())
                .encode()
                .toUriString();

        String eTag = kernel.exchange(uri, HttpMethod.GET, null, Organization.class, user()).getHeaders().getETag();

        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", eTag);

        kernel.exchange(uri, HttpMethod.PUT, new HttpEntity<Organization>(org, headers), Void.class, user());
    }
}
