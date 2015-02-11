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

    String id;
    String name;
    Instant modified;
    OrganizationType type;
    @JsonProperty("territory_id")
    URI territoryId;

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
}
