package org.oasis_eu.spring.datacore.model;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * User: flombard
 * Date: 24/2/14
 */
public class DCRights {

    private LinkedHashSet<String> owners;
    private LinkedHashSet<String> writers;
    private LinkedHashSet<String> readers;

    public LinkedHashSet<String> getOwners() {
        return owners;
    }
    public void setOwners(LinkedHashSet<String> owners) {
        this.owners = owners;
    }

    public void addOwners(Collection<String> owners) {
        this.owners.addAll(owners);
    }

    public LinkedHashSet<String> getWriters() {
        return writers;
    }
    public void setWriters(LinkedHashSet<String> writers) {
        this.writers = writers;
    }

    public LinkedHashSet<String> getReaders() {
        return readers;
    }
    public void setReaders(LinkedHashSet<String> readers) {
        this.readers = readers;
    }

}
