package org.oasis_eu.spring.kernel.service;

import org.oasis_eu.spring.kernel.model.Organization;

/**
 * User: schambon
 * Date: 6/25/14
 */
public interface OrganizationStore {

    Organization find(String id);

}
