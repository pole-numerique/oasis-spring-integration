package org.oasis_eu.spring.kernel.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.Instant;

/**
 * User: schambon
 * Date: 6/13/14
 */
public class InboundNotification {
    @JsonProperty("id")
    String id;
    @JsonProperty("user_id")
    String userId;

    @JsonProperty("instance_id")
    String instanceId;
    @JsonProperty("service_id")
    String serviceId;

    @JsonProperty("message")
    String message;

    @JsonProperty("action_uri")
    String actionUri;

    @JsonProperty("action_label")
    String actionLabel;

    @JsonProperty("status")
    NotificationStatus status;

    @JsonProperty("time")
    Instant time;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getActionUri() {
        return actionUri;
    }

    public void setActionUri(String actionUri) {
        this.actionUri = actionUri;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public void setActionLabel(String actionLabel) {
        this.actionLabel = actionLabel;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }
}
