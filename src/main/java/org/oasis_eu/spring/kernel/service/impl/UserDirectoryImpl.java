package org.oasis_eu.spring.kernel.service.impl;

import com.nimbusds.jose.util.Base64;

import org.joda.time.Instant;
import org.oasis_eu.spring.kernel.exception.TechnicalErrorException;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.model.directory.OrgMembership;
import org.oasis_eu.spring.kernel.model.directory.UserMembership;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.oasis_eu.spring.kernel.service.UserDirectory;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.model.directory.AgentInfo;
import org.oasis_eu.spring.kernel.model.directory.Group;
import org.oasis_eu.spring.kernel.security.OpenIdCConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Collections;
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
    private static final String GROUP_MEMBER_REMOVE_PATH = "/group/{groupId}/members/{agentId}";

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

        HttpEntity<AgentInfo[]> response = kernelRestTemplate.exchange(agentsUri, HttpMethod.GET, new HttpEntity<>(initHeaders()), AgentInfo[].class);

        if (LOGGER.isDebugEnabled()) {
            for (AgentInfo agentInfo : response.getBody()) {
                LOGGER.debug("> {}", agentInfo);
            }
        }



        return Arrays.asList(response.getBody());
    }

    @Override
    public AgentInfo getAgent(String agentId) {
        String agentUri = UriComponentsBuilder.fromUriString(userDirectoryEndpoint)
                .path(AGENT_PATH)
                .buildAndExpand(agentId)
                .encode()
                .toUriString();

        HttpEntity<AgentInfo> response = kernelRestTemplate.exchange(agentUri, HttpMethod.GET, new HttpEntity<>(initHeaders()), AgentInfo.class);
        return response.getBody();
    }

    @Override
    public void createAgent(String organizationId, UserInfo userInfo) {
        String groupMembersUri = UriComponentsBuilder.fromUriString(userDirectoryEndpoint)
                .path(AGENTS_PATH)
                .buildAndExpand(organizationId)
                .encode()
                .toUriString();
        kernelRestTemplate.exchange(groupMembersUri, HttpMethod.POST, new HttpEntity<>(userInfo, initHeaders()), Void.class);
    }

    @Override
    public void deleteAgent(AgentInfo agentInfo) {
        String groupMembersUri = UriComponentsBuilder.fromUriString(userDirectoryEndpoint)
                .path(AGENT_PATH)
                .buildAndExpand(agentInfo.getId())
                .encode()
                .toUriString();
        
        HttpHeaders headers = initHeaders();
        headers.set("If-Match", Long.toString(agentInfo.getModified()));
        
        kernelRestTemplate.exchange(groupMembersUri, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
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
        
        HttpEntity<Group> req = new HttpEntity<>(group, initHeaders());
        
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
        kernelRestTemplate.exchange(groupUri, HttpMethod.DELETE, new HttpEntity<Void>(initHeaders()), Void.class);
    }

    @Override
    public void addAgentToGroup(String agentId, String groupId) {
        String groupMembersUri = UriComponentsBuilder.fromUriString(userDirectoryEndpoint)
                .path(GROUP_MEMBERS_PATH)
                .buildAndExpand(groupId)
                .encode()
                .toUriString();
        kernelRestTemplate.exchange(groupMembersUri, HttpMethod.POST, new HttpEntity<>(agentId, initHeaders()), Void.class);
    }

    @Override
    public void removeAgentfromGroup(String agentId, String groupId) {
        String groupMembersUri = UriComponentsBuilder.fromUriString(userDirectoryEndpoint)
                .path(GROUP_MEMBER_REMOVE_PATH)
                .buildAndExpand(groupId, agentId)
                .encode()
                .toUriString();
        kernelRestTemplate.exchange(groupMembersUri, HttpMethod.DELETE, new HttpEntity<>(initHeaders()), Void.class);
    }

	private HttpHeaders initHeaders() {
        HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", String.format("Basic %s", Base64.encode(String.format("%s:%s", configuration.getClientId(), configuration.getClientSecret()))));
		return headers;
	}


    @Override
    public List<UserMembership> getMembershipsOfUser(String userId) {

        HttpHeaders headers = new HttpHeaders();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication instanceof OpenIdCAuthentication) {
            headers.set("Authorization", String.format("Bearer %s", ((OpenIdCAuthentication) authentication).getAccessToken()));
        }

        String uriString = UriComponentsBuilder.fromHttpUrl(userDirectoryEndpoint)
                .path("/memberships/user/{user_id}")
                .queryParam("start", "0")
                .queryParam("limit", "20")
                .buildAndExpand(userId)
                .toUriString();

        ResponseEntity<UserMembership[]> response = kernelRestTemplate.exchange(uriString, HttpMethod.GET, new HttpEntity<>(headers), UserMembership[].class);
        if (response.getStatusCode().is2xxSuccessful()) {
            String orgs = "";
            for (UserMembership m : response.getBody()) {
                orgs += String.format("%s (%s)\n", m.getOrganizationName(), m.getOrganizationId());
            }
            LOGGER.debug("Found memberships in the following organizations:\n{}", orgs);

            return Arrays.asList(response.getBody());
        } else {
            LOGGER.error("Cannot load user membership information: {}", response.getStatusCode());

            if (response.getStatusCode().is4xxClientError()) {
                throw new WrongQueryException();
            } else {
                throw new TechnicalErrorException();
            }
        }


    }

    @Override
    public List<OrgMembership> getMembershipsOfOrganization(String organizationId) {
        HttpHeaders headers = new HttpHeaders();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication instanceof OpenIdCAuthentication) {
            headers.set("Authorization", String.format("Bearer %s", ((OpenIdCAuthentication) authentication).getAccessToken()));
        }

        ResponseEntity<OrgMembership[]> response = kernelRestTemplate.exchange(userDirectoryEndpoint + "/memberships/org/{organization_id}", HttpMethod.GET, new HttpEntity<Object>(headers), OrgMembership[].class, organizationId);
        if (response.getStatusCode().is2xxSuccessful()) {
            return Arrays.asList(response.getBody());
        } else {
            LOGGER.error("Cannot load organization memberships: {}", response.getStatusCode());
            if (response.getStatusCode().is4xxClientError()) {
                throw new WrongQueryException();
            } else {
                throw new TechnicalErrorException();
            }
        }
    }
}
