package org.oasis_eu.spring.kernel.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.oasis_eu.spring.kernel.KernelTestConstants;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.oasis_eu.spring.test.IntegrationTest;
import org.oasis_eu.spring.test.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertNotEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= TestConfiguration.class, loader = AnnotationConfigContextLoader.class)
public class NotificationServiceTest {

    @Autowired
    private RestTemplate kernelRestTemplate;

    @Autowired
    private NotificationService service;

    @Test
    public void testGetNotifications() {
        String response = "[{\"id\":\"ae59bfb4-7c6b-473a-b510-0f713fc362cc\",\"userId\":\"bb2c6f76-362f-46aa-982c-1fc60d54b8ef\",\"applicationId\":\"0a046fde-a20f-46eb-8252-48b78d89a9a2\",\"message\":\"Votre demande a bien été prise en compte dans notre système.\\n\\nVous pouvez consulter votre requête à l'adresse suivante : http://www-xanadu.citizenkin.com/valence/form/electoral_roll_registration/53cfa6f2e4b0a314227f0e57\",\"time\":1406117619177,\"status\":\"UNREAD\",\"modified\":1406117619177}]";
        MockRestServiceServer mock = MockRestServiceServer.createServer(kernelRestTemplate);
        mock.expect(anything()).andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        assertNotEquals(-1, service.getNotifications(KernelTestConstants.USER_ALICE).size());

        mock.verify();
    }

    @Test
    public void testGetNotificationsForApp() {
        String response = "[{\"id\":\"ae59bfb4-7c6b-473a-b510-0f713fc362cc\",\"userId\":\"bb2c6f76-362f-46aa-982c-1fc60d54b8ef\",\"applicationId\":\"0a046fde-a20f-46eb-8252-48b78d89a9a2\",\"message\":\"Votre demande a bien été prise en compte dans notre système.\\n\\nVous pouvez consulter votre requête à l'adresse suivante : http://www-xanadu.citizenkin.com/valence/form/electoral_roll_registration/53cfa6f2e4b0a314227f0e57\",\"time\":1406117619177,\"status\":\"UNREAD\",\"modified\":1406117619177}]";
        MockRestServiceServer mock = MockRestServiceServer.createServer(kernelRestTemplate);
        mock.expect(anything()).andRespond(withSuccess(response, MediaType.APPLICATION_JSON));


        assertNotEquals(-1, service.getAppNotifications(
        		KernelTestConstants.USER_ALICE, KernelTestConstants.APP_CITIZEN_KIN).size());
    }

//    @Test
//    public void testSetAllNotificationsRead() {
//        List<String> messageIds = service.getAppNotifications(ALICE, CITIZEN_KIN).stream().map(n -> n.getId()).collect(Collectors.toList());
//        service.setMessageStatus(ALICE, messageIds, NotificationStatus.READ);
//
//        service.getAppNotifications(ALICE, CITIZEN_KIN).forEach(n -> assertEquals(NotificationStatus.READ, n.getStatus()));
//    }

    @Before
    public void setupAuthenticationContext() {
        OpenIdCAuthentication authentication = new OpenIdCAuthentication("test", "accesstoken", "idtoken", java.time.Instant.now(), java.time.Instant.now());
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
        SecurityContextHolder.getContext().setAuthentication(authentication);

    }
}