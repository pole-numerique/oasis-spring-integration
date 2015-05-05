package org.oasis_eu.spring.datacore.impl;

import org.junit.runner.RunWith;
import org.oasis_eu.spring.datacore.DatacoreClient;
import org.oasis_eu.spring.test.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * User: schambon
 * Date: 5/5/15
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class}, loader = AnnotationConfigContextLoader.class)
@DirtiesContext /* further tests should be made in a new application context */
abstract public class BaseDatacoreClientTest {
    @Autowired
    @Qualifier("dataCore")
    protected RestTemplate dataCoreRestTemplate;
    @Autowired
    protected DatacoreClient datacoreClient;

    protected MockRestServiceServer setupMockServerForGetResources() throws IOException {
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
}
