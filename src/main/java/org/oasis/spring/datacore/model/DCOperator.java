package org.oasis.spring.datacore.model;

/**
 * User: schambon
 * Date: 1/3/14
 */
public enum DCOperator {


    EQ(""),
    GT(">"),
    GTE(">="),
    LT("<"),
    LTE("<="),
    NE("<>"),
    IN("$in"),
    NIN("$nin"),
    REGEX("$regex"),
    EXISTS("$exists");

    private final String representation;

    private DCOperator(String representation) {
        this.representation = representation;
    }


    public String getRepresentation() {
        return representation;
    }
}
