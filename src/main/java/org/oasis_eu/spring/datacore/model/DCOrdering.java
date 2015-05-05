package org.oasis_eu.spring.datacore.model;

/**
 * User: schambon
 * Date: 4/24/15
 */
public enum DCOrdering {
    ASCENDING("+"),
    DESCENDING("-");

    private String representation;

    private DCOrdering(String representation) {
        this.representation = representation;
    }

    public String representation() {
        return representation;
    }
}
