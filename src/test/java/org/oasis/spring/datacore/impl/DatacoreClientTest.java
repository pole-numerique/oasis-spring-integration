package org.oasis.spring.datacore.impl;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis.spring.datacore.DatacoreClient;
import org.oasis.spring.datacore.model.DCOperator;
import org.oasis.spring.datacore.model.DCQueryParameters;
import org.oasis.spring.datacore.model.DCResource;
import org.oasis.spring.test.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * User: schambon
 * Date: 1/3/14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class}, loader = AnnotationConfigContextLoader.class)
@DirtiesContext /* further tests should be made in a new application context */
public class DatacoreClientTest {

    @Autowired
    @Qualifier("dataCore")
    private RestTemplate dataCoreRestTemplate;

    @Autowired
    private DatacoreClient datacoreClient;

    @Test
    public void testUriBuilder() {
        UriComponents components = UriComponentsBuilder.fromUriString("http://localhost:8080").path("/dc/type/{type}").build().expand("citizenkin.procedure.envelope").encode();

        URI uri = components.toUri();

        assertEquals("http://localhost:8080/dc/type/citizenkin.procedure.envelope", uri.toString());

    }

    @Test
    public void testGetResourcesLowLevel() throws IOException {

        MockRestServiceServer mockRestServiceServer = setupMockServerForGetResources();


        URI uri =
                UriComponentsBuilder.fromUriString("http://localhost:8080")
                        .path("/dc/type/{type}")
                        .build()
                        .expand("citizenkin.procedure.envelope")
                        .encode()
                        .toUri();

        DCResource[] resources = dataCoreRestTemplate.getForObject(uri, DCResource[].class);

        assertEquals(2, resources.length);
        assertEquals("33333_4444_5555", resources[0].getIri());

        mockRestServiceServer.verify();
    }

    private MockRestServiceServer setupMockServerForGetResources() throws IOException {
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(dataCoreRestTemplate);

        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("dc_inner/multiple_resources.json")));
        String line = reader.readLine();
        StringBuilder b = new StringBuilder();
        while (line != null) {
            b.append(line);
            b.append("\n");
            line = reader.readLine();
        }

        String response = b.toString();

        mockServer.expect(requestTo("http://localhost:8080/dc/type/citizenkin.procedure.envelope")).andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
        return mockServer;
    }

    @Test
    public void testGetResources() throws IOException {
        MockRestServiceServer mockRestServiceServer = setupMockServerForGetResources();

        List<DCResource> resources = datacoreClient.findResources("citizenkin.procedure.envelope");
        assertEquals(2, resources.size());
        assertEquals("33333_4444_5555", resources.get(0).getIri());

        mockRestServiceServer.verify();
    }


    @Test
    public void testFindResourcesWithStartAndLimit()  throws Exception {

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(dataCoreRestTemplate);

        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("dc_inner/multiple_resources.json")));
        String line = reader.readLine();
        StringBuilder b = new StringBuilder();
        while (line != null) {
            b.append(line);
            b.append("\n");
            line = reader.readLine();
        }

        String response = b.toString();

        mockServer.expect(requestTo("http://localhost:8080/dc/type/citizenkin.procedure.envelope?start=0&limit=10"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        List<DCResource> resources = datacoreClient.findResources("citizenkin.procedure.envelope", null, 0, 10);
        assertEquals(2, resources.size());
        assertEquals("33333_4444_5555", resources.get(0).getIri());

        mockServer.verify();
    }


    @Test
    public void testFindResourcesWithQueryParameters()  throws Exception {

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(dataCoreRestTemplate);

        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("dc_inner/multiple_resources.json")));
        String line = reader.readLine();
        StringBuilder b = new StringBuilder();
        while (line != null) {
            b.append(line);
            b.append("\n");
            line = reader.readLine();
        }

        String response = b.toString();

        mockServer.expect(requestTo("http://localhost:8080/dc/type/citizenkin.procedure.envelope?start=0&limit=10&recipient=%22organization:val%22&definition_name=%22electoral_roll_registration%22"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        DCQueryParameters parameters = new DCQueryParameters("recipient", DCOperator.EQ, "organization:val")
                .and("definition_name", DCOperator.EQ, "electoral_roll_registration");

        List<DCResource> resources = datacoreClient.findResources("citizenkin.procedure.envelope", parameters, 0, 10);
        assertEquals(2, resources.size());
        assertEquals("33333_4444_5555", resources.get(0).getIri());

        mockServer.verify();
    }


    @Test
    public void testSaveResource() throws IOException {

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(dataCoreRestTemplate);

        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("dc_inner/resource.json")));
        String line = reader.readLine();
        StringBuilder b = new StringBuilder();
        while (line != null) {
            b.append(line);
            b.append("\n");
            line = reader.readLine();
        }

        String response = b.toString();

        mockServer.expect(requestTo("http://localhost:8080/dc/type/citizenkin.procedure.envelope"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Content-Type", "application/json"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        DCResource resource = new DCResource();
        resource.setType("citizenkin.procedure.envelope");
        resource.setIri("3333_4444_5555");
        resource.setBaseUri("http://data-test.oasis-eu.org/dc/type");
        resource.setCreated(new DateTime().withDate(2010, 01, 01).withTime(23, 30, 30, 0));
        resource.getValues().put("state", new DCResource.StringValue("SENT"));
        resource.getValues().put("definition_name", new DCResource.StringValue("electoral_roll_registration"));
        resource.getValues().put("initiator", new DCResource.StringValue("tagada-tsouin-tsouin"));

        datacoreClient.saveResource(resource);


        mockServer.verify();
    }

    @Test
    public void testUpdateResource() {
        DCResource resource = new DCResource();
        resource.setType("citizenkin.procedure.envelope");
        resource.setIri("3333_4444_5555");
        resource.setBaseUri("http://data-test.oasis-eu.org/dc/type");
        resource.setCreated(new DateTime().withDate(2010, 01, 01).withTime(23, 30, 30, 0));
        resource.getValues().put("state", new DCResource.StringValue("SENT"));
        resource.getValues().put("definition_name", new DCResource.StringValue("electoral_roll_registration"));
        resource.getValues().put("initiator", new DCResource.StringValue("tagada-tsouin-tsouin"));
        resource.setVersion(0);

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(dataCoreRestTemplate);
        mockServer.expect(requestTo("http://localhost:8080/dc/type/citizenkin.procedure.envelope/3333_4444_5555"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess());

        datacoreClient.updateResource(resource);

        mockServer.verify();
    }

    @Test
    public void testDeleteResource() {
        DCResource resource = new DCResource();
        resource.setType("citizenkin.procedure.envelope");
        resource.setIri("3333_4444_5555");
        resource.setBaseUri("http://data-test.oasis-eu.org/dc/type");
        resource.setCreated(new DateTime().withDate(2010, 01, 01).withTime(23, 30, 30, 0));
        resource.getValues().put("state", new DCResource.StringValue("SENT"));
        resource.getValues().put("definition_name", new DCResource.StringValue("electoral_roll_registration"));
        resource.getValues().put("initiator", new DCResource.StringValue("tagada-tsouin-tsouin"));
        resource.setVersion(0);

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(dataCoreRestTemplate);
        mockServer.expect(requestTo("http://localhost:8080/dc/type/citizenkin.procedure.envelope/3333_4444_5555"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withNoContent());

        datacoreClient.deleteResource(resource);

        mockServer.verify();
    }
}
