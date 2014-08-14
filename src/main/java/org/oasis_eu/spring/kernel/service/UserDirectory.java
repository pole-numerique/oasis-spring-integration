package org.oasis_eu.spring.kernel.service;

import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.model.directory.AgentInfo;
import org.oasis_eu.spring.kernel.model.directory.Group;
import org.oasis_eu.spring.kernel.model.directory.OrgMembership;
import org.oasis_eu.spring.kernel.model.directory.UserMembership;

import java.util.List;

public interface UserDirectory {

    List<AgentInfo> getAgents(String organizationId, int start, int limit);

    AgentInfo getAgent(String agentId);

	void createAgent(String organizationId, UserInfo userInfo);

	void deleteAgent(AgentInfo agentInfo);

    Group createGroup(String organizationId, String name);

    void deleteGroup(Group group);

    void addAgentToGroup(String agentId, String groupId);

	void removeAgentfromGroup(String agentId, String groupId);

    List<UserMembership> getMembershipsOfUser(String userId);

    List<OrgMembership> getMembershipsOfOrganization(String organizationId);

}
