package org.oasis_eu.spring.kernel.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User: schambon
 * Date: 6/13/14
 */
public class OutboundNotification {
    @JsonProperty("user_ids")
    private String[] userIds;

    @JsonProperty("service_id")
    private String serviceId;

    @JsonProperty("message")
    private String message;

    @JsonProperty("action_uri")
    private String actionUri;

    @JsonProperty("action_label")
    private String actionLabel;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String[] getUserIds() {
        return userIds;
    }

    public void setUserIds(String[] userIds) {
        this.userIds = userIds;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
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
}
