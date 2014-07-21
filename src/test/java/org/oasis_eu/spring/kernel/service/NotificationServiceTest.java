package org.oasis_eu.spring.kernel.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.spring.kernel.KernelTestConstants;
import org.oasis_eu.spring.test.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.Assert.assertNotEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= TestConfiguration.class, loader = AnnotationConfigContextLoader.class)
public class NotificationServiceTest {

    @Autowired
    private NotificationService service;

    @Test
    public void testGetNotifications() {

        assertNotEquals(-1, service.getNotifications(KernelTestConstants.USER_ALICE).size());

    }

    @Test
    public void testGetNotificationsForApp() {
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
}