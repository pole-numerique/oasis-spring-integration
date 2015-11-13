package org.oasis_eu.spring.datacore.impl;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import org.junit.Test;
import org.oasis_eu.spring.datacore.model.DCOperator;
import org.oasis_eu.spring.datacore.model.DCOrdering;
import org.oasis_eu.spring.datacore.model.DCQueryParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * User: schambon
 * Date: 5/5/15
 */
public class OrderedDatacoreQueryTest extends BaseDatacoreClientTest {
    @Value("${application.geoarea.project:geo_0}")
    private String project;

    // Someday we'll have to tighten exception handling in the dc client
    // (eg return an empty resource list when there is a 404)
    @Test(expected = HttpClientErrorException.class)
    public void testOrderedQuery() {

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(dataCoreRestTemplate);
        mockServer
                .expect(requestTo(UriComponentsBuilder
                        .fromHttpUrl("http://localhost:8080/dc/type/a_type")
                        .queryParam("start", 0)
                        .queryParam("limit", 100)
                        .queryParam("name", "+")
                        .build()
                        .encode()
                        .toString()))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        DCQueryParameters params = new DCQueryParameters("name", DCOrdering.ASCENDING);
        datacoreClient.findResources(project, "a_type", params, 0, 100);
    }

    @Test(expected = HttpClientErrorException.class)
    public void orderedQueryWithParameter() {
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(dataCoreRestTemplate);
        mockServer
                .expect(requestTo(UriComponentsBuilder
                    .fromHttpUrl("http://localhost:8080/dc/type/a_type")
                    .queryParam("start", 100)
                    .queryParam("limit", 200)
                    .queryParam("parameter", ">600-")
                    .build()
                    .encode()
                    .toString()))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        datacoreClient.findResources(project, "a_type", new DCQueryParameters("parameter", DCOrdering.DESCENDING, DCOperator.GT, "600"), 100, 200);
    }
}
