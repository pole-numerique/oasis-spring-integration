package org.oasis_eu.spring.kernel.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.joda.time.Instant;

/**
 * User: schambon
 * Date: 6/25/14
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Organization {

    String id;
    String name;
    Instant modified;
    OrganizationType type;

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
}
