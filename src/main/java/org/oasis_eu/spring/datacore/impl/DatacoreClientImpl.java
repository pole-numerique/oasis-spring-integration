package org.oasis_eu.spring.datacore.impl;

import java.net.URI;
import java.net.URISyntaxException;
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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
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
                        .encode() // ex. orgprfr:OrgPriv%C3%A9e_0 (WITH unencoded ':' and encoded accented chars etc.)
                        .toUri();
        LOGGER.debug("Fetching ALL Resources: URI String is " + uri);

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
                // ex. {start=[0], limit=[11], geo:name.v=[$regex^Zamor], geo:country=[http://data.ozwillo.com/dc/type/geocoes:Pa%C3%ADs_0/ES]}
            }
        }
        UriComponents uriComponents = uriComponentsBuilder
                .build()
                .expand(model)
                .encode();
        // path ex. orgprfr:OrgPriv%C3%A9e_0 (WITH unencoded ':' and encoded accented chars etc.)
        // and query ex. geo:name.v=$regex%5EZamor&geo:country=http://data.ozwillo.com/dc/type/geocoes:Pa%25C3%25ADs_0/ES
        // NB. This will also encode all parameters including the regex ^ and other matches like "geo:country=http..." which is wrong

        URI requestUri = uriComponents.toUri();
        // and NOT uriComponents.toString() else variable expansion encodes it once too many
        // (because new UriTemplate(uriString) assumes uriString is not yet encoded -_-)
        // ex. https://plnm-dev-dc/dc/type/geoci:City_0?start=0&limit=11&geo:name.v=$regex%5EZamor&geo:country=http://data.ozwillo.com/dc/type/geocoes:Pa%25C3%25ADs_0/ES 

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fetching limited Resources: URI String is " + requestUri);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Datacore-Project", project.trim());
            DCResource[] dcResource = dataCoreRestTemplate.exchange(requestUri, HttpMethod.GET, new HttpEntity<>(headers), DCResource[].class).getBody();

            return Arrays.asList(dcResource);
        } catch(HttpClientErrorException ex) {
            LOGGER.error("Error caught while querying data core", ex);
            LOGGER.debug("Response body: {}", ex.getResponseBodyAsString());
            throw ex;
        }

    }


    @Override
    public DCResult getResource(String project, String model, String iri) {
        URI uri = dcResourceUri(model, iri);

        LOGGER.debug("Fetching Resource: URI String is " + uri);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Datacore-Project", project);
            DCResource resource = dataCoreRestTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), DCResource.class).getBody();
            return new DCResult(DCResultType.SUCCESS, resource);
        } catch (HttpClientErrorException e) {
            return this.getDCResultFromHttpErrorException(e);
        }
    }

    @Override
    public DCResult getResourceFromURI(String project, String url) {
        /* Below is a solution for issue 268 : it happened only in PROD because a not well ENCODED URL was decoded and verified by the HAProxy,
         * then when it found any remaining special characters (like encoded spaces) it responded with a 400 Bad Request error. 
         * In this case, the url after being decoded by HAproxy was : http://data.ozwillo.com/dc/type/geocibg:НаселеноMесто_0/BG/BG-02/Малко%20Търново
         * In Dev / Preprod there is not any HAProxy and the java datacore app treat the request in case it have encoded (or doubled encoded) chars.
         * In previous code the URL was assumed to be well encoded changing only the datacoreUrl (resources in DC have baseUrl http://data.ozwillo.com and not
         *  https://data.ozwillo.com NOR https://data.ozwillo-dev.eu > real DC addresses) before submit it in the request, which was a wrong approach. */
        DCResource dummyResourceForUri = new DCResource();
        dummyResourceForUri.setUri(url);
        URI uri = dcResourceUri(dummyResourceForUri.getType(), dummyResourceForUri.getIri());

        LOGGER.debug("Fetching Resource From URI String : " + uri);

        try {
            HttpHeaders headers = getCommonHeaders(project);
            DCResource resource = dataCoreRestTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), DCResource.class).getBody();
            return new DCResult(DCResultType.SUCCESS, resource);
        } catch (HttpClientErrorException e) {
            return this.getDCResultFromHttpErrorException(e);
        }
    }

    @Override
    public List<String> getResourceAliases(String project, String type, String iri) {
        URI uri = UriComponentsBuilder.fromUriString(datacoreUrl)
            .path("/dc/aliases/{type}/{iri}")
            .build()
            .expand(type, iri)
            .encode()
            .toUri();

        LOGGER.debug("Searching aliases for {}", uri);

        HttpHeaders headers = getCommonHeaders(project);
        String[] aliases = dataCoreRestTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String[].class).getBody();

        return Arrays.asList(aliases);
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
                .encode() // ex. orgprfr:OrgPriv%C3%A9e_0 (WITH unencoded ':' and encoded accented chars etc.)
                .toUri();
        LOGGER.debug("Saving into Resource: URI String is " + uri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Datacore-Project", project);

        HttpEntity<DCResource> requestEntity = new HttpEntity<>(resource, headers);

        try {
            ResponseEntity<DCResource> entity = dataCoreRestTemplate.exchange(uri, HttpMethod.POST, requestEntity, DCResource.class);

            return new DCResult(DCResultType.fromCode(entity.getStatusCode().value()), entity.getBody());
        } catch (HttpClientErrorException e) {
            return this.getDCResultFromHttpErrorException(e);
        }
    }

    @Override
    public DCResult updateResource(String project, DCResource resource) {
        URI uri = dcResourceUri(resource);
        LOGGER.debug("Updating Resource: URI String is " + uri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Datacore-Project", project);

        HttpEntity<DCResource> requestEntity = new HttpEntity<>(resource, headers);

        try {
            dataCoreRestTemplate.put(uri, requestEntity);
            return new DCResult(DCResultType.SUCCESS, (DCResource) null);
        } catch (HttpClientErrorException e) {
            return this.getDCResultFromHttpErrorException(e);
        }

    }

    @Override
    public DCResult deleteResource(String project, DCResource resource) {
        URI uri = dcResourceUri(resource);
        LOGGER.debug("Deleting Resource: URI String is " + uri);

        HttpHeaders headers = new HttpHeaders();
        headers.set("If-Match", Integer.toString(resource.getVersion()));
        headers.set("X-Datacore-Project", project);

        ResponseEntity<Object> entity = dataCoreRestTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<>(headers), Object.class);

        return new DCResult(DCResultType.fromCode(entity.getStatusCode().value()), (DCResource) null);
    }

    @Override
    public DCResult addRightsOnResource(String project, DCResource resource, DCRights rights) {
        URI uri = dcResourceRightsUri(resource);
        LOGGER.debug("Adding rights into Resource: URI String is " + uri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Datacore-Project", project);

        HttpEntity<DCRights> requestEntity = new HttpEntity<>(rights, headers);

        try {
            dataCoreRestTemplate.exchange(uri, HttpMethod.POST, requestEntity, Void.class);
            return new DCResult(DCResultType.SUCCESS, (DCResource) null);
        } catch (HttpClientErrorException e) {
            return this.getDCResultFromHttpErrorException(e);
        }
    }

    @Override
    public DCResult getRightsOnResource(String project, DCResource resource) {
        URI uri = dcResourceRightsUri(resource);
        LOGGER.debug("Fetching Resource: URI String is " + uri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Datacore-Project", project);

        try {
            DCRights rights = dataCoreRestTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), DCRights.class).getBody();
            return new DCResult(DCResultType.SUCCESS, rights);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return this.getDCResultFromHttpErrorException(e);
        }
    }

    @Override
    public DCResult setRightsOnResource(String project, DCResource resource, DCRights rights) {
        URI uri = dcResourceRightsUri(resource);
        LOGGER.debug("Setting rights to Resource: URI String is " + uri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Datacore-Project", project);

        HttpEntity<DCRights> requestEntity = new HttpEntity<>(rights, headers);

        try {
            dataCoreRestTemplate.put(uri, requestEntity);
            return new DCResult(DCResultType.SUCCESS, (DCResource) null);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return this.getDCResultFromHttpErrorException(e);
        }
    }


    /* HELPERS & HANDLERS */

    private DCResult getDCResultFromHttpErrorException(HttpStatusCodeException e){
        LOGGER.error("Error caught while querying data core", e);
        LOGGER.debug("Response body: {}", e.getResponseBodyAsString());
        return new DCResult(DCResultType.fromCode(e.getStatusCode().value()), e.getResponseBodyAsString());
    }

    private StringBuilder dcResourceTypeUriBuilder(String resourceType, String apiUriPart) {
        // we use StringBuilder rather than the Spring fluent URI builder API, because
        // it has no way to expand already encoded path components :
        /*String uriString = UriComponentsBuilder.fromUriString(datacoreUrl)
                .path("/dc/r/{type}")
                .build()
                // NB. error if type is already encoded (OrgPriv%C3%A9e_0 gives: OrgPriv%25C325%A9e_0)
                .expand(resource.getType())
                // AND NOT path("/dc/r/{type}/{iri}/{version}")....expand(DCResource.encodeUriPathSegment(resource.getType()) rather than encode() else double encoded
                // ACTUALLY A SPRING-LIKE SOLUTION WOULD BE .pathSegment(iri.split("/")).expand(...).encode()...
                .encode()
                .toUriString();
        StringBuilder sb = new StringBuilder(uriString);*/
        StringBuilder sb = new StringBuilder(datacoreUrl);
        sb.append(apiUriPart);
        sb.append(DCResource.encodeUriPathSegment(resourceType));
        return sb;
    }

    private StringBuilder dcResourceUriBuilder(DCResource resource, String apiUriPart) {
        return dcResourceUriBuilder(resource.getType(), resource.getIri(), apiUriPart);
    }

    private StringBuilder dcResourceUriBuilder(String resourceType, String resourceIri, String apiUriPart) {
        StringBuilder sb = dcResourceTypeUriBuilder(resourceType, apiUriPart);
        sb.append('/');
        sb.append(resourceIri); // already encoded
        return sb;
    }

    private URI dcResourceRightsUri(DCResource resource) {
        StringBuilder sb = dcResourceUriBuilder(resource, "/dc/r/");
        sb.append('/');
        sb.append(resource.getVersion()); // no need to encode
        return builderToUri(sb);
    }

    private URI dcResourceUri(DCResource resource) {
        return builderToUri(dcResourceUriBuilder(resource, "/dc/type/"));
    }

    private URI dcResourceUri(String resourceType, String iri) {
        return builderToUri(dcResourceUriBuilder(resourceType, iri, "/dc/type/"));
    }

    /**
     * avoids URISyntaxException
     * @param sb
     * @return
     */
    private URI builderToUri(StringBuilder sb) {
        try {
            return new URI(sb.toString());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private HttpHeaders getCommonHeaders(String project) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Datacore-Project", project);
        return headers;
    }
}
