package org.oasis.spring.datacore.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 1/6/14
 */
public class DCResult {

    DCResultType type;
    DCResource resource; // may be null
    List<String> errorMessages = new ArrayList<>();

    public DCResult(DCResultType type, DCResource resource) {
        this.type = type;
        this.resource = resource;
    }


    public DCResult(DCResultType type, String... errors) {
        this.type = type;

        errorMessages = Arrays.stream(errors).collect(Collectors.toList());

    }

    public DCResource getResource() {
        return resource;
    }

    public DCResultType getType() {
        return type;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
