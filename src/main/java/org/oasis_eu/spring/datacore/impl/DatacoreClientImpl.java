package org.oasis_eu.spring.datacore.impl;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.oasis_eu.spring.datacore.DatacoreClient;
import org.oasis_eu.spring.datacore.model.DCQueryParameters;
import org.oasis_eu.spring.datacore.model.DCResource;
import org.oasis_eu.spring.datacore.model.DCResult;
import org.oasis_eu.spring.datacore.model.DCResultType;
import org.oasis_eu.spring.datacore.model.DCRights;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

    @Value("${datacore.url: http://localhost:8080}")
    private String datacoreUrl;

    /**
     * Note: this only returns the results that the DC returns… i.e. the first 10 by default.
     * So this method is only a proof-of-concept really.
     * @param model
     * @return
     */
    @Override
    public List<DCResource> findResources(String project, String model) {

        URI uri = UriComponentsBuilder.fromUriString(datacoreUrl)
                        .path("/dc/type/{type}")
                        .build()
                        .expand(model)
                        .encode()
                        .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Datacore-Project", project);
        DCResource[] resources = dataCoreRestTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), DCResource[].class).getBody();

        return Arrays.asList(resources);
    }

    @Override
    public List<DCResource> findResources(String project, String model, DCQueryParameters queryParameters, int start, int maxResult) {

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(datacoreUrl)
                .path("/dc/type/{type}")
                .queryParam("start", start)
                .queryParam("limit", maxResult);

        if (queryParameters != null) {
            for (DCQueryParameters.DCQueryParam param : queryParameters) {
                uriComponentsBuilder.queryParam(param.getSubject(), param.getOperator().getRepresentation() + param.getObject());
            }
        }

        String uriString = uriComponentsBuilder
                              .build()
                              .expand(model)
//                            .encode()
                              .toUriString();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Find Resources: URI String is " + uriString);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Datacore-Project", project.trim());
            DCResource[] dcResource = dataCoreRestTemplate.exchange(uriString, HttpMethod.GET, new HttpEntity<>(headers), DCResource[].class).getBody();

            return Arrays.asList(dcResource);
        } catch(HttpClientErrorException ex) {
            LOGGER.error("Error caught while querying data core", ex);
            LOGGER.debug("Response body: {}", ex.getResponseBodyAsString());
            throw ex;
        }

    }

    @Override
    public DCResult getResource(String project, String model, String iri) {

        URI uri = UriComponentsBuilder.fromUriString(datacoreUrl)
                .path("/dc/type/{type}/{iri}")
                .build()
                .expand(model, iri)
                .encode()
                .toUri();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Datacore-Project", project);
            DCResource resource = dataCoreRestTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), DCResource.class).getBody();
            return new DCResult(DCResultType.SUCCESS, resource);
        } catch (HttpClientErrorException e) {
            return new DCResult(DCResultType.fromCode(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }
    @Override
    public DCResult getResourceFromURI(String project, String url) {
        URI uri = UriComponentsBuilder.fromUriString(url)
                .build()
                .encode()
                .toUri();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Datacore-Project", project);
            DCResource resource = dataCoreRestTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), DCResource.class).getBody();
            return new DCResult(DCResultType.SUCCESS, resource);
        } catch (HttpClientErrorException e) {
            return new DCResult(DCResultType.fromCode(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }

    @Override
    public DCResult saveResource(String project, DCResource resource) {

        if (!resource.isNew()) {
            LOGGER.error("Calls to saveResource must only be made on new resources");
            return new DCResult(DCResultType.CONFLICT, "Calls to saveResource must only be made on new resources");
        }

        URI uri = UriComponentsBuilder.fromUriString(datacoreUrl)
                .path("/dc/type/{type}")
                .build()
                .expand(resource.getType())
                //.encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Datacore-Project", project);

        HttpEntity<DCResource> requestEntity = new HttpEntity<>(resource, headers);

        try {
            ResponseEntity<DCResource> entity = dataCoreRestTemplate.exchange(uri, HttpMethod.POST, requestEntity, DCResource.class);

            return new DCResult(DCResultType.fromCode(entity.getStatusCode().value()), entity.getBody());
        } catch (HttpClientErrorException e) {
            return new DCResult(DCResultType.fromCode(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }

    @Override
    public DCResult updateResource(String project, DCResource resource) {
        URI uri = UriComponentsBuilder.fromUriString(datacoreUrl)
                .path("/dc/type/{type}/{iri}")
                .build()
                .expand(resource.getType(), resource.getIri())
                //.encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Datacore-Project", project);

        HttpEntity<DCResource> requestEntity = new HttpEntity<>(resource, headers);

        try {
            dataCoreRestTemplate.put(uri, requestEntity);
            return new DCResult(DCResultType.SUCCESS, (DCResource) null);
        } catch (HttpClientErrorException e) {
            return new DCResult(DCResultType.fromCode(e.getStatusCode().value()), e.getResponseBodyAsString());
        }

    }

    @Override
    public DCResult deleteResource(String project, DCResource resource) {

        URI uri = UriComponentsBuilder.fromUriString(datacoreUrl)
                .path("/dc/type/{type}/{iri}")
                .build()
                .expand(resource.getType(), resource.getIri())
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("If-Match", Integer.toString(resource.getVersion()));
        headers.set("X-Datacore-Project", project);

        ResponseEntity<Object> entity = dataCoreRestTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<>(headers), Object.class);

        return new DCResult(DCResultType.fromCode(entity.getStatusCode().value()), (DCResource) null);
    }

    @Override
    public DCResult addRightsOnResource(String project, DCResource resource, DCRights rights) {
        URI uri = UriComponentsBuilder.fromUriString(datacoreUrl)
                .path("/dc/r/{type}/{iri}/{version}")
                .build()
                .expand(resource.getType(), resource.getIri(), resource.getVersion())
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Datacore-Project", project);

        HttpEntity<DCRights> requestEntity = new HttpEntity<>(rights, headers);

        try {
            dataCoreRestTemplate.exchange(uri, HttpMethod.POST, requestEntity, Void.class);
            return new DCResult(DCResultType.SUCCESS, (DCResource) null);
        } catch (HttpClientErrorException e) {
            return new DCResult(DCResultType.fromCode(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }

    @Override
    public DCResult getRightsOnResource(String project, DCResource resource) {
        URI uri = UriComponentsBuilder.fromUriString(datacoreUrl)
                .path("/dc/r/{type}/{iri}/{version}")
                .build()
                //error if type is already encoded (OrgPriv%C3%A9e_0 gives: OrgPriv%25C325%A9e_0)
                .expand(resource.getType(), resource.getIri(), resource.getVersion())
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Datacore-Project", project);

        try {
            DCRights rights = dataCoreRestTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), DCRights.class).getBody();
            return new DCResult(DCResultType.SUCCESS, rights);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return new DCResult(DCResultType.fromCode(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }

    @Override
    public DCResult setRightsOnResource(String project, DCResource resource, DCRights rights) {
        URI uri = UriComponentsBuilder.fromUriString(datacoreUrl)
                .path("/dc/r/{type}/{iri}/{version}")
                //.path("/dc/r/"+resource.getType() +"/"+ resource.getIri() +"/"+ resource.getVersion() )
                .build()
                .expand(resource.getType(), resource.getIri(), resource.getVersion())
                //.encode() //should not be encoded
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Datacore-Project", project);

        HttpEntity<DCRights> requestEntity = new HttpEntity<>(rights, headers);

        try {
            dataCoreRestTemplate.put(uri, requestEntity);
            return new DCResult(DCResultType.SUCCESS, (DCResource) null);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return new DCResult(DCResultType.fromCode(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }

}
