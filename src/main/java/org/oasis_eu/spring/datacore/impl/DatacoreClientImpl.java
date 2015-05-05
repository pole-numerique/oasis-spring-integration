package org.oasis_eu.spring.datacore.impl;

import com.google.gson.Gson;
import org.oasis_eu.spring.datacore.DatacoreClient;
import org.oasis_eu.spring.datacore.model.*;
import org.oasis_eu.spring.kernel.exception.TechnicalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * Low-level datacore client
 * Eventually will support caching
 *
 * User: schambon
 * Date: 1/2/14
 */
@Component
public class DatacoreClientImpl implements DatacoreClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatacoreClientImpl.class);

    @Autowired
    @Qualifier("dataCore")
    private RestTemplate dataCoreRestTemplate;

    @Value("${datacore.url:http://localhost:8080}")
    private String datacoreUrl;

    /**
     * Note: this only returns the results that the DC returns… i.e. the first 10 by default.
     * So this method is only a proof-of-concept really.
     * @param model
     * @return
     */
    @Override
    public List<DCResource> findResources(String model) {

        URI uri =
                UriComponentsBuilder.fromUriString(datacoreUrl)
                        .path("/dc/type/{type}")
                        .build()
                        .expand(model)
                        .encode()
                        .toUri();

        DCResource[] resources = dataCoreRestTemplate.getForObject(uri, DCResource[].class);

        return Arrays.asList(resources);
    }

    @Override
    public List<DCResource> findResources(String model, DCQueryParameters queryParameters, int start, int maxResult) {

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(datacoreUrl)
                .path("/dc/type/{type}")
                .queryParam("start", start)
                .queryParam("limit", maxResult);

        if (queryParameters != null) {
            for (DCQueryParameters.DCQueryParam param : queryParameters) {
                uriComponentsBuilder.queryParam(param.getSubject(), param.getOperator().getRepresentation() + param.getObject());
            }
        }

        String uriString =
                uriComponentsBuilder
                        .build()
                        .expand(model)
//                        .encode()
                        .toUriString();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Find Resources: URI String is " + uriString);

        }

        try {
            return Arrays.asList(dataCoreRestTemplate.getForObject(uriString, DCResource[].class));
        } catch(HttpClientErrorException ex) {
            LOGGER.error("Error caught while querying data core", ex);

            LOGGER.debug("Response body: {}", ex.getResponseBodyAsString());

            throw ex;
        }

    }

    @Override
    public DCResult getResource(String model, String iri) {

        URI uri = UriComponentsBuilder.fromUriString(datacoreUrl)
                .path("/dc/type/{type}/{iri}")
                .build()
                .expand(model, iri)
                .encode()
                .toUri();

        try {
            DCResource resource = dataCoreRestTemplate.getForObject(uri, DCResource.class);
            return new DCResult(DCResultType.SUCCESS, resource);
        } catch (HttpClientErrorException e) {
            return new DCResult(DCResultType.fromCode(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }

    @Override
    public DCResult saveResource(DCResource resource) {

        if (!resource.isNew()) {
            LOGGER.error("Calls to saveResource must only be made on new resources");
            return new DCResult(DCResultType.CONFLICT, "Calls to saveResource must only be made on new resources");
        }

        URI uri = UriComponentsBuilder.fromUriString(datacoreUrl)
                .path("/dc/type/{type}")
                .build()
                .expand(resource.getType())
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<DCResource> requestEntity = new HttpEntity<>(resource, headers);

        try {
            ResponseEntity<DCResource> entity = dataCoreRestTemplate.exchange(uri, HttpMethod.POST, requestEntity, DCResource.class);

            return new DCResult(DCResultType.fromCode(entity.getStatusCode().value()), entity.getBody());
        } catch (HttpClientErrorException e) {
            return new DCResult(DCResultType.fromCode(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }

    @Override
    public DCResult updateResource(DCResource resource) {
        URI uri = UriComponentsBuilder.fromUriString(datacoreUrl)
                .path("/dc/type/{type}/{iri}")
                .build()
                .expand(resource.getType(), resource.getIri())
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<DCResource> requestEntity = new HttpEntity<>(resource, headers);

        try {
            dataCoreRestTemplate.put(uri, requestEntity);
            return new DCResult(DCResultType.SUCCESS, (DCResource) null);
        } catch (HttpClientErrorException e) {
            return new DCResult(DCResultType.fromCode(e.getStatusCode().value()), e.getResponseBodyAsString());
        }

    }

    @Override
    public DCResult deleteResource(DCResource resource) {

        URI uri = UriComponentsBuilder.fromUriString(datacoreUrl)
                .path("/dc/type/{type}/{iri}")
                .build()
                .expand(resource.getType(), resource.getIri())
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("If-Match", Integer.toString(resource.getVersion()));

        ResponseEntity<Object> entity = dataCoreRestTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<>(headers), Object.class);

        return new DCResult(DCResultType.fromCode(entity.getStatusCode().value()), (DCResource) null);
    }

    @Override
    public DCResult addRightsOnResource(DCResource resource, DCRights rights) {
        URI uri = UriComponentsBuilder.fromUriString(datacoreUrl)
                .path("/dc/r/{type}/{iri}/{version}")
                .build()
                .expand(resource.getType(), resource.getIri(), resource.getVersion())
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<DCRights> requestEntity = new HttpEntity<>(rights, headers);

        try {
            dataCoreRestTemplate.exchange(uri, HttpMethod.POST, requestEntity, Void.class);
            return new DCResult(DCResultType.SUCCESS, (DCResource) null);
        } catch (HttpClientErrorException e) {
            return new DCResult(DCResultType.fromCode(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }

    @Override
    public DCResult setRightsOnResource(DCResource resource, DCRights rights) {
        URI uri = UriComponentsBuilder.fromUriString(datacoreUrl)
                .path("/dc/r/{type}/{iri}/{version}")
                .build()
                .expand(resource.getType(), resource.getIri(), resource.getVersion())
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<DCRights> requestEntity = new HttpEntity<>(rights, headers);

        try {
            dataCoreRestTemplate.put(uri, requestEntity);
            return new DCResult(DCResultType.SUCCESS, (DCResource) null);
        } catch (HttpClientErrorException e) {
            return new DCResult(DCResultType.fromCode(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }

}
