package org.oasis_eu.spring.kernel.service.impl;

import com.nimbusds.jose.util.Base64;
import org.joda.time.Instant;
import org.oasis_eu.spring.kernel.service.UserDirectory;
import org.oasis_eu.spring.kernel.model.directory.AgentInfo;
import org.oasis_eu.spring.kernel.model.directory.Group;
import org.oasis_eu.spring.kernel.security.OpenIdCConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class UserDirectoryImpl implements UserDirectory {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDirectoryImpl.class);

    @Value("${kernel.user_directory_endpoint}")
    private String userDirectoryEndpoint;

    private static final String AGENTS_PATH = "/org/{organizationId}/agents";
    private static final String AGENT_PATH = "/agent/{agentId}";
    private static final String GROUPS_PATH = "/org/{organizationId}/groups";
    private static final String GROUP_PATH = "/group/{groupId}";
    private static final String GROUP_MEMBERS_PATH = "/group/{groupId}/members";

    @Autowired
    @Qualifier("kernelRestTemplate")
    private RestTemplate kernelRestTemplate;

    @Autowired
    private OpenIdCConfiguration configuration;

    @Override
    public List<AgentInfo> getAgents(String organizationId, int start, int limit) {
        String agentsUri = UriComponentsBuilder.fromUriString(userDirectoryEndpoint)
                .path(AGENTS_PATH)
                .queryParam("start", start)
                .queryParam("limit", limit)
                .buildAndExpand(organizationId)
                .encode()
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Basic %s", Base64.encode(String.format("%s:%s", configuration.getClientId(), configuration.getClientSecret()))));

        LOGGER.debug("Calling " + agentsUri);

        HttpEntity<AgentInfo[]> response = kernelRestTemplate.exchange(agentsUri, HttpMethod.GET, new HttpEntity<>(headers), AgentInfo[].class);
        return Arrays.asList(response.getBody());
    }

    @Override
    public AgentInfo getAgent(String agentId) {
        String agentUri = UriComponentsBuilder.fromUriString(userDirectoryEndpoint)
                .path(AGENT_PATH)
                .buildAndExpand(agentId)
                .encode()
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Basic %s", Base64.encode(String.format("%s:%s", configuration.getClientId(), configuration.getClientSecret()))));

        HttpEntity<AgentInfo> response = kernelRestTemplate.exchange(agentUri, HttpMethod.GET, new HttpEntity<>(headers), AgentInfo.class);
        return response.getBody();
    }

    @Override
    public Group createGroup(String organizationId, String name) {
        String groupsUri = UriComponentsBuilder.fromUriString(userDirectoryEndpoint)
                .path(GROUPS_PATH)
                .buildAndExpand(organizationId)
                .encode()
                .toUriString();
        Group group = new Group();
        group.setName(name);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", String.format("Basic %s", Base64.encode(String.format("%s:%s", configuration.getClientId(), configuration.getClientSecret()))));
        
        HttpEntity<Group> req = new HttpEntity<>(group, headers);
        
        ResponseEntity<Void> resp = kernelRestTemplate.exchange(groupsUri, HttpMethod.POST, req, Void.class);
        
        Pattern groupUriRegexp = Pattern.compile("/d/group/(.*)");
        Matcher m = groupUriRegexp.matcher(resp.getHeaders().getLocation().getPath());
        if (m.matches()) {
            group.setId(m.group(1));
        } else {
            throw new IllegalStateException("Resulting group URI not recognized");
        }

        group.setModified(Instant.now().withMillis(Long.valueOf(resp.getHeaders().getETag())));

        return group;
    }

    @Override
    public void deleteGroup(Group group) {
        String groupUri = UriComponentsBuilder.fromUriString(userDirectoryEndpoint)
                .path(GROUP_PATH)
                .buildAndExpand(group.getId())
                .encode()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("If-Match", Long.toString(group.getModified().getMillis()));
        headers.set("Authorization", String.format("Basic %s", Base64.encode(String.format("%s:%s", configuration.getClientId(), configuration.getClientSecret()))));
        kernelRestTemplate.exchange(groupUri, HttpMethod.DELETE, new HttpEntity<Void>(headers), Void.class);
    }

    @Override
    public void addAgentToGroup(String agentId, String groupId) {
        String groupMembersUri = UriComponentsBuilder.fromUriString(userDirectoryEndpoint)
                .path(GROUP_MEMBERS_PATH)
                .buildAndExpand(groupId)
                .encode()
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Basic %s", Base64.encode(String.format("%s:%s", configuration.getClientId(), configuration.getClientSecret()))));
        kernelRestTemplate.exchange(groupMembersUri, HttpMethod.POST, new HttpEntity<>(agentId, headers), Void.class);
    }


}
