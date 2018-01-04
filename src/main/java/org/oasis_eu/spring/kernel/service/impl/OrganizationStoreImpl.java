package org.oasis_eu.spring.kernel.service.impl;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;
import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.userIfExists;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.oasis_eu.spring.kernel.model.Organization;
import org.oasis_eu.spring.kernel.model.OrganizationStatus;
import org.oasis_eu.spring.kernel.rest.ResponseProviderInterceptor;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.collect.ImmutableMap;

/**
 * User: schambon
 * Date: 6/25/14
 */
@Service
public class OrganizationStoreImpl implements OrganizationStore {

    @Autowired
    private Kernel kernel;

    @Value("${kernel.organization_endpoint}")
    private String organizationEndpoint;


    @Override
    @Cacheable("organizations")
    public Organization find(String organizationId) {
        String uri = UriComponentsBuilder.fromUriString(organizationEndpoint)
                .path("/{organizationId}")
                .build()
                .toUriString();

        return kernel.getEntityOrNull(uri, Organization.class, userIfExists(), organizationId);
    }

    @SuppressWarnings("static-access")
    @Override
    public Organization findByDCID(String dc_id) {
        String uri = UriComponentsBuilder.fromUriString(organizationEndpoint)
                .queryParam("dc_id", dc_id)
                .build()
                .toUriString();
        Organization entity = kernel.getEntityOrNull(uri, Organization.class, userIfExists());

        //Remove message since its not required and error is already treated
        if(entity == null){
            HttpServletResponse appResponse = ResponseProviderInterceptor.getResponse();
            if (appResponse != null) {
                appResponse.setHeader(Kernel.RESPONSE_HEADER_NAME, "");
            }
        }
        return entity;
    }

    @Override
    @CachePut(value = "organizations", key = "#result.id")
    public Organization create(Organization organization) {

        String uri = UriComponentsBuilder.fromUriString(organizationEndpoint)
                .build()
                .toUriString();

         ResponseEntity<Organization> kernelResp = kernel.exchange(uri, HttpMethod.POST, 
                     new HttpEntity<Organization>(organization), Organization.class, user());
        // validate response body
        return kernel.getBodyUnlessClientError(kernelResp, Organization.class, uri);
    }

    @Override
    @CacheEvict(value = "organizations", key = "#org.id")
    public void update(Organization org) {
        String uri = UriComponentsBuilder.fromUriString(organizationEndpoint)
                .path("/{organizationId}")
                .buildAndExpand(org.getId())
                .encode()
                .toUriString();

        String eTag = kernel.exchange(uri, HttpMethod.GET, null, Organization.class, user()).getHeaders().getETag();

        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", eTag);

        ResponseEntity<Void> kernelResp = kernel.exchange(uri, HttpMethod.PUT, new HttpEntity<Organization>(org, headers), Void.class, user());
        // validate response body
        kernel.getBodyUnlessClientError(kernelResp, Void.class, uri);

    }

    @Override
    @CacheEvict(value = "organizations", key = "#organizationId")
    public String setStatus(String organizationId, OrganizationStatus status) {
        String uri = UriComponentsBuilder.fromUriString(organizationEndpoint)
                .path("/{organizationId}")
                .buildAndExpand(organizationId)
                .encode()
                .toUriString();

        String eTag = kernel.exchange(uri, HttpMethod.GET, null, Organization.class, user()).getHeaders().getETag();

        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", eTag);

        Map<String,String> statusMap = new ImmutableMap.Builder<String, String>().put("status", status.toString()) .build();
        ResponseEntity<String> resEntity = kernel.exchange(uri, HttpMethod.POST,
                new HttpEntity<Map<String,String>>(statusMap, headers), String.class, user());

        /*  DONT CHANGE BELOW code unless updating front-end app since there is a pop up linked to this message */
        // specific error handling, TODO LATER make it more consistent with generic error handling
        if (resEntity.getStatusCode().is4xxClientError() || resEntity.getStatusCode().is5xxServerError()) {
            String res = resEntity.getBody();
            if (res != null && !res.trim().isEmpty()) {
                return res; // error message if any
            }
        }
        return null;
    }
    
}
