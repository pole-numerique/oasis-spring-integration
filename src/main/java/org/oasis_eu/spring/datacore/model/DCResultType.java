package org.oasis_eu.spring.datacore.model;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * User: schambon
 * Date: 1/6/14
 */
public enum DCResultType {

    SUCCESS(200, 201, 204),
    NOT_FOUND(404),
    CONFLICT(409),
    BAD_REQUEST(400),
    FORBIDDEN(403),
    SERVER_ERROR(500),
    NOT_MODIFIED(304),
    UNKNOWN();


    private final ImmutableSet<Integer> codes;

    DCResultType(Integer... codes_) {
        this.codes = ImmutableSet.copyOf(Sets.newHashSet(codes_));
    }

    public Set<Integer> getCodes() {
        return codes;
    }

    public static DCResultType fromCode(int code) {
        for (DCResultType type : DCResultType.values()) {
            if (type.getCodes().contains(code)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
