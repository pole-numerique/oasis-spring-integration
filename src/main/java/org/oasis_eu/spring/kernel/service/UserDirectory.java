package org.oasis_eu.spring.kernel.service;

import org.oasis_eu.spring.kernel.model.directory.AgentInfo;
import org.oasis_eu.spring.kernel.model.directory.Group;

import java.util.List;

public interface UserDirectory {

    List<AgentInfo> getAgents(String organizationId, int start, int limit);

    AgentInfo getAgent(String agentId);

    Group createGroup(String organizationId, String name);

    void deleteGroup(Group group);

    void addAgentToGroup(String agentId, String groupId);

}
