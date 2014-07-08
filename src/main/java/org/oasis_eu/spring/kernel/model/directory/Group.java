package org.oasis_eu.spring.kernel.model.directory;


import org.joda.time.Instant;

public class Group {

    private String id;
    private String name;
    private Instant modified;

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

    public void setModified(Instant modified) {
        this.modified = modified;
    }
    public Instant getModified() {
        return modified;
    }

}
