package org.oasis_eu.spring.datacore.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DCModel {

    @JsonProperty("@id")
    private URI id;

    @JsonProperty("dcmo:name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "DCModel{" +
            "id=" + id +
            '}';
    }
}
