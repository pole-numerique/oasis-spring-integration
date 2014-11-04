package org.oasis_eu.spring.kernel.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.spring.kernel.KernelTestConstants;
import org.oasis_eu.spring.kernel.model.NotificationStatus;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.oasis_eu.spring.test.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class, loader = AnnotationConfigContextLoader.class)
public class NotificationServiceTest {

    @Autowired
    private RestTemplate kernelRestTemplate;

    @Autowired
    private NotificationService service;

    @Test
    public void testGetNotifications() {
        String response = "[{\"id\":\"ae59bfb4-7c6b-473a-b510-0f713fc362cc\",\"user_id\":\"bb2c6f76-362f-46aa-982c-1fc60d54b8ef\",\"instance_id\":\"0a046fde-a20f-46eb-8252-48b78d89a9a2\",\"service_id\":\"1b157fef-a20f-46eb-8252-48b78d89a9a2\",\"message\":\"Votre demande a bien été prise en compte dans notre système.\",\"action_uri\":\"https://www.citizenkin.com/toto\",\"action_label\":\"Voir votre demande\",\"time\":1406117619177,\"status\":\"UNREAD\"}]";
        MockRestServiceServer mock = MockRestServiceServer.createServer(kernelRestTemplate);
        mock.expect(anything()).andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        assertEquals(1, service.getNotifications(KernelTestConstants.USER_ALICE, NotificationStatus.UNREAD).size());

        mock.verify();
    }

    @Test
    public void testGetNotificationsForApp() {
        String response = "[{\"id\":\"ae59bfb4-7c6b-473a-b510-0f713fc362cc\",\"user_id\":\"bb2c6f76-362f-46aa-982c-1fc60d54b8ef\",\"instance_id\":\"0a046fde-a20f-46eb-8252-48b78d89a9a2\",\"service_id\":\"1b157fef-a20f-46eb-8252-48b78d89a9a2\",\"message\":\"Votre demande a bien été prise en compte dans notre système.\",\"action_uri\":\"https://www.citizenkin.com/toto\",\"action_label\":\"Voir votre demande\",\"time\":1406117619177,\"status\":\"UNREAD\"}]";
        MockRestServiceServer mock = MockRestServiceServer.createServer(kernelRestTemplate);
        mock.expect(anything()).andRespond(withSuccess(response, MediaType.APPLICATION_JSON));


        assertEquals(1, service.getInstanceNotifications(
                KernelTestConstants.USER_ALICE, KernelTestConstants.APP_CITIZEN_KIN, NotificationStatus.ANY).size());
    }


    @Before
    public void setupAuthenticationContext() {
        OpenIdCAuthentication authentication = new OpenIdCAuthentication("test", "accesstoken", "idtoken", java.time.Instant.now(), java.time.Instant.now(), true, false);
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
        SecurityContextHolder.getContext().setAuthentication(authentication);

    }
}