package org.oasis_eu.spring.kernel.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.spring.config.KernelConfiguration;
import org.oasis_eu.spring.kernel.model.instance.ScopeNeeded;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.*;
import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.none;
import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = KernelTest.class, loader = AnnotationConfigContextLoader.class)
@Configuration
@ComponentScan(basePackages = "org.oasis_eu.spring")
@Import(KernelConfiguration.class)
@PropertySource("classpath:test-configuration.properties")
public class KernelTest {

    @Autowired
    private RestTemplate kernelRestTemplate;

    @Autowired
    private Kernel kernel;

    @Test
    public void testUserAuth() throws Exception {
        String response = "{\"scope_id\":\"test\", \"motivation\":\"some reason\"}";

        MockRestServiceServer mock = MockRestServiceServer.createServer(kernelRestTemplate);
        mock.expect(anything())
                .andExpect(header("Authorization", "Bearer accesstoken"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        kernel.exchange("http://whatever", HttpMethod.GET, null, ScopeNeeded.class, user("accesstoken"));

        mock.verify();

    }

    @Test
    public void testNoAuth() throws Exception {
        String response = "{\"scope_id\":\"test\", \"motivation\":\"some reason\"}";

        MockRestServiceServer mock = MockRestServiceServer.createServer(kernelRestTemplate);
        mock.expect(anything())
                .andExpect(request -> assertNull(request.getHeaders().get("Authorization")))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        kernel.exchange("http://whatever", HttpMethod.GET, null, ScopeNeeded.class, none());

        mock.verify();
    }

}