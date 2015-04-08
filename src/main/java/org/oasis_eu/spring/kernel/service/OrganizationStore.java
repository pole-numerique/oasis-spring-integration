package org.oasis_eu.spring.kernel.service;

import org.oasis_eu.spring.kernel.model.Organization;
import org.oasis_eu.spring.kernel.model.OrganizationStatus;

/**
 * User: schambon
 * Date: 6/25/14
 * 
 * To delete, rather trash by POSTing with a DELETED status
 */
public interface OrganizationStore {

    Organization find(String id);

    Organization create(Organization organization);

    void update(Organization org);

    /**
     * for trash mode
     * @param organizationId
     * @param status
     * @return (error, warning...) message if any
     */
    String setStatus(String organizationId, OrganizationStatus status);
}
