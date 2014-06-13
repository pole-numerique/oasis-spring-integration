package org.oasis_eu.spring.kernel.service;

import org.oasis_eu.spring.kernel.model.InboundNotification;
import org.oasis_eu.spring.kernel.model.OutboundNotification;
import org.oasis_eu.spring.kernel.model.NotificationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * User: schambon
 * Date: 6/13/14
 */
@Service
public class NotificationService {

    @Autowired
    private RestTemplate kernelRestTemplate;

    @Value("${kernel.notifications_endpoint}")
    private String endpoint;

    public void sendNotification(OutboundNotification outboundNotification) {
        kernelRestTemplate.postForEntity(endpoint + "/publish", outboundNotification, String.class);
    }

    public List<org.oasis_eu.spring.kernel.model.InboundNotification> getNotifications(String userId) {
        return Arrays.asList(kernelRestTemplate.getForObject(endpoint + "/{user_id}/messages", InboundNotification[].class, userId));
    }

    public List<InboundNotification> getAppNotifications(String userId, String appId) {
        URI uri = UriComponentsBuilder.fromHttpUrl(endpoint)
                .path("/{user_id}/messages")
                .queryParam("appId", appId)
                .buildAndExpand(userId)
                .toUri();

        return Arrays.asList(kernelRestTemplate.getForObject(uri, InboundNotification[].class));
    }

    public void setMessageStatus(String userId, List<String> messageIds, NotificationStatus status) {
        MessageStatus ms = new MessageStatus();
        ms.setMessageIds(messageIds);
        ms.setStatus(status);
        kernelRestTemplate.postForEntity(endpoint + "/{user_id}/messages", ms, String.class, userId);
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
