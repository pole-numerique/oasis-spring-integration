package org.oasis_eu.spring.kernel.service;

import org.oasis_eu.spring.kernel.model.directory.OrgMembership;
import org.oasis_eu.spring.kernel.model.directory.UserMembership;

import java.util.List;

public interface UserDirectory {



    List<UserMembership> getMembershipsOfUser(String userId);

    List<OrgMembership> getMembershipsOfOrganization(String organizationId);

}
