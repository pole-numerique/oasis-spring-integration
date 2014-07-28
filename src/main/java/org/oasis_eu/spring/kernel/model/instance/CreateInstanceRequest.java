package org.oasis_eu.spring.kernel.model.instance;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * User: schambon
 * Date: 7/1/14
 */
public class CreateInstanceRequest {

    @JsonProperty("instance_id")
    private String instanceId;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("organization_id")
    private String organizationId;

    @JsonProperty("organization_name")
    private String organizationName;

    @JsonProperty("instance_registration_uri")
    private String instanceRegistrationUri;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getInstanceRegistrationUri() {
        return instanceRegistrationUri;
    }

    public void setInstanceRegistrationUri(String instanceRegistrationUri) {
        this.instanceRegistrationUri = instanceRegistrationUri;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    @Override
    public String toString() {
        return "CreateInstanceRequest{" +
                "instanceId='" + instanceId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", userId='" + userId + '\'' +
                ", organizationId='" + organizationId + '\'' +
                ", organizationName='" + organizationName + '\'' +
                ", instanceRegistrationUri='" + instanceRegistrationUri + '\'' +
                '}';
    }
}
