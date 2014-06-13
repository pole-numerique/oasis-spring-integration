package org.oasis_eu.spring.kernel.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.spring.kernel.model.NotificationStatus;
import org.oasis_eu.spring.test.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= TestConfiguration.class, loader = AnnotationConfigContextLoader.class)
public class NotificationServiceTest {

    @Autowired
    private NotificationService service;

    private static final String ALICE = "bb2c6f76-362f-46aa-982c-1fc60d54b8ef";
    private static final String CITIZEN_KIN = "0a046fde-a20f-46eb-8252-48b78d89a9a2";

    @Test
    public void testGetNotifications() {

        assertNotEquals(-1, service.getNotifications(ALICE).size());

    }

    @Test
    public void testGetNotificationsForApp() {
        assertNotEquals(-1, service.getAppNotifications(ALICE, CITIZEN_KIN).size());
    }

//    @Test
//    public void testSetAllNotificationsRead() {
//        List<String> messageIds = service.getAppNotifications(ALICE, CITIZEN_KIN).stream().map(n -> n.getId()).collect(Collectors.toList());
//        service.setMessageStatus(ALICE, messageIds, NotificationStatus.READ);
//
//        service.getAppNotifications(ALICE, CITIZEN_KIN).forEach(n -> assertEquals(NotificationStatus.READ, n.getStatus()));
//    }
}