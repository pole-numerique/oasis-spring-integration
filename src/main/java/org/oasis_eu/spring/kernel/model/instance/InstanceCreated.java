package org.oasis_eu.spring.kernel.model.instance;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * User: schambon
 * Date: 7/1/14
 */
public class InstanceCreated {

    @JsonProperty("instance_id")
    private String instanceId;

    @JsonProperty("services")
    private List<ServiceCreated> services = new ArrayList<>();

    @JsonProperty("scopes")
    private List<ScopeCreated> scopes = new ArrayList<>();

    @JsonProperty("needed_scopes")
    private List<ScopeNeeded> neededScopes = new ArrayList<>();

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public List<ServiceCreated> getServices() {
        return services;
    }

    public void setServices(List<ServiceCreated> services) {
        this.services = services;
    }

    public List<ScopeCreated> getScopes() {
        return scopes;
    }

    public void setScopes(List<ScopeCreated> scopes) {
        this.scopes = scopes;
    }

    public List<ScopeNeeded> getNeededScopes() {
        return neededScopes;
    }

    public void setNeededScopes(List<ScopeNeeded> neededScopes) {
        this.neededScopes = neededScopes;
    }

    @Override
    public String toString() {
        return "InstanceCreated{" +
                "instanceId='" + instanceId + '\'' +
                ", services=" + services +
                ", scopes=" + scopes +
                ", neededScopes=" + neededScopes +
                '}';
    }
}
