package org.oasis_eu.spring.kernel.service;

import org.oasis_eu.spring.kernel.model.UserAccount;
import org.oasis_eu.spring.kernel.model.directory.OrgMembership;
import org.oasis_eu.spring.kernel.model.directory.UserMembership;

import java.util.List;

public interface UserDirectory {


    List<UserMembership> getMembershipsOfUser(String userId);

    List<OrgMembership> getMembershipsOfOrganization(String organizationId);

    List<OrgMembership> getMembershipsOfOrganization(String organizationId, int start, int limit);

    List<OrgMembership> getAdminsOfOrganization(String organizationId);

    void updateMembership(UserMembership um, boolean admin, String userId);

    void updateMembership(OrgMembership om, boolean admin, String organizationId);

    void saveUserAccount(UserAccount userAccount);

    UserAccount findUserAccount(String id);

    void removeMembership(OrgMembership orgMembership, String organizationId);

    void createMembership(String email, String organizationId);

    void removeMembership(UserMembership userMembership, String userId);
}
