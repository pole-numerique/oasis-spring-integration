package org.oasis_eu.spring.kernel.service;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.client;
import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.model.InboundNotification;
import org.oasis_eu.spring.kernel.model.NotificationStatus;
import org.oasis_eu.spring.kernel.model.OutboundNotification;
import org.oasis_eu.spring.kernel.security.OpenIdCConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User: schambon
 * Date: 6/13/14
 */
@Service
public class NotificationService {

    //private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private Kernel kernel;

    @Autowired
    private OpenIdCConfiguration configuration;

    @Value("${kernel.notifications_endpoint}")
    private String endpoint;

    /**
     * NOT USED FOR NOW
     * @param outboundNotification
     * @throws WrongQueryException
     */
    public void sendNotification(OutboundNotification outboundNotification) throws WrongQueryException{
        String uriString = endpoint + "/publish";
        ResponseEntity<Void> kernelResp = kernel.exchange(uriString, HttpMethod.POST, new HttpEntity<Object>(outboundNotification), 
                Void.class, client(configuration.getClientId(), configuration.getClientSecret()));
        // validate response body
        kernel.getBodyUnlessClientError(kernelResp, Void.class, uriString); // TODO test
    }

    public List<InboundNotification> getNotifications(String userId, NotificationStatus status) {
        return getInstanceNotifications(userId, null, status);
    }

    public List<InboundNotification> getInstanceNotifications(String userId, String instanceId, NotificationStatus status) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(endpoint)
                .path("/{user_id}/messages");

        if (instanceId != null) {
            builder.queryParam("instance", instanceId);
        }

        if (!status.equals(NotificationStatus.ANY)) {
            builder.queryParam("status", status.toString());
        }

        String uri = builder
                .buildAndExpand(userId)
                .toUriString();

        InboundNotification[] notifsEntity = kernel.getEntityOrNull(uri, InboundNotification[].class, user());

        if (notifsEntity == null || notifsEntity.length < 1 ) {
            return Collections.emptyList();
        }

        return Arrays.asList(notifsEntity);
        
    }

    public void setMessageStatus(String userId, List<String> messageIds, NotificationStatus status) throws WrongQueryException{
        MessageStatus ms = new MessageStatus();
        ms.setMessageIds(messageIds);
        ms.setStatus(status);

        String uriString = endpoint + "/{user_id}/messages";
        ResponseEntity<Void> kernelResp = kernel.exchange(uriString, HttpMethod.POST, new HttpEntity<Object>(ms), Void.class, user(), userId);
        // validate response body
        kernel.getBodyUnlessClientError(kernelResp, Void.class, uriString); // TODO test
    }

    static class MessageStatus {
        NotificationStatus status;
        @JsonProperty("message_ids")
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
