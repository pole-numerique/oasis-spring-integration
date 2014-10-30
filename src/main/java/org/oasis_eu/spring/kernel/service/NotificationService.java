package org.oasis_eu.spring.kernel.service;

import org.oasis_eu.spring.kernel.model.InboundNotification;
import org.oasis_eu.spring.kernel.model.NotificationStatus;
import org.oasis_eu.spring.kernel.model.OutboundNotification;
import org.oasis_eu.spring.kernel.security.OpenIdCConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.client;
import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;

/**
 * User: schambon
 * Date: 6/13/14
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private Kernel kernel;

    @Autowired
    private OpenIdCConfiguration configuration;

    @Value("${kernel.notifications_endpoint}")
    private String endpoint;

    public void sendNotification(OutboundNotification outboundNotification) {
        kernel.exchange(endpoint + "/publish", HttpMethod.POST, new HttpEntity<Object>(outboundNotification), Void.class, client(configuration.getClientId(), configuration.getClientSecret()));
    }

    public List<InboundNotification> getNotifications(String userId) {
        InboundNotification[] notifs = kernel.getForObject(endpoint + "/{user_id}/messages", InboundNotification[].class, user(), userId);

        return notifs == null ? Collections.emptyList() : Arrays.asList(notifs);
    }

    public List<InboundNotification> getAppNotifications(String userId, String appId) {
        String uri = UriComponentsBuilder.fromHttpUrl(endpoint)
                .path("/{user_id}/messages")
                .queryParam("appId", appId)
                .buildAndExpand(userId)
                .toUriString();

        ResponseEntity<InboundNotification[]> notifsEntity = kernel.exchange(uri, HttpMethod.GET, null, InboundNotification[].class, user());

        if (!notifsEntity.getStatusCode().is2xxSuccessful()) {
            logger.warn("Getting app notifications resulted in {} {}", notifsEntity.getStatusCode().value(), notifsEntity.getStatusCode().getReasonPhrase());
            return Collections.emptyList();
        }

        return Arrays.asList(notifsEntity.getBody());
    }

    public void setMessageStatus(String userId, List<String> messageIds, NotificationStatus status) {
        MessageStatus ms = new MessageStatus();
        ms.setMessageIds(messageIds);
        ms.setStatus(status);
        kernel.exchange(endpoint + "/{user_id}/messages", HttpMethod.POST, new HttpEntity<Object>(ms), Void.class, user(), userId);
    }

    static class MessageStatus {
        NotificationStatus status;
        List<String> messageIds;

        public NotificationStatus getStatus() {
            return status;
        }

        public void setStatus(NotificationStatus status) {
            this.status = status;
        }

        public List<String> getMessageIds() {
            return messageIds;
        }

        public void setMessageIds(List<String> messageIds) {
            this.messageIds = messageIds;
        }
    }
}
