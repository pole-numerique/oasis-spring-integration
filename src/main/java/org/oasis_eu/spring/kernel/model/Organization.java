package org.oasis_eu.spring.kernel.model;

import java.io.Serializable;
import java.net.URI;

import org.joda.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User: schambon
 * Date: 6/25/14
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Organization implements Serializable {
    private static final long serialVersionUID = 5334020951283823727L;
    
    String id;
    String name;
    Instant modified;
    OrganizationType type;
    /** optional */
    @JsonProperty("territory_id")
    URI territoryId;
    @JsonProperty("dc_id")
    URI dcId;
    OrganizationStatus status;

    /** optional */
    @JsonProperty("status_changed")
    Instant statusChanged;
    /** optional */
    @JsonProperty("status_change_requester_id")
    String statusChangeRequesterId;

   public Instant getModified() {
        return modified;
    }

    public void setModified(Instant modified) {
        this.modified = modified;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OrganizationType getType() {
        return type;
    }

    public void setType(OrganizationType type) {
        this.type = type;
    }

    public URI getTerritoryId() {
        return territoryId;
    }

    public void setTerritoryId(URI territoryId) {
        this.territoryId = territoryId;
    }

    public URI getDcId() {
        return dcId;
    }

    public void setDcId(URI dcId) {
        this.dcId = dcId;
    }

    public OrganizationStatus getStatus() {
        return status;
    }

    public void setStatus(OrganizationStatus status) {
        this.status = status;
    }

    public Instant getStatusChanged() {
        return statusChanged;
    }

    public void setStatusChanged(Instant statusChanged) {
        this.statusChanged = statusChanged;
    }

    public String getStatusChangeRequesterId() {
        return statusChangeRequesterId;
    }

    public void setStatusChangeRequesterId(String statusChangeRequesterId) {
        this.statusChangeRequesterId = statusChangeRequesterId;
    }

}
