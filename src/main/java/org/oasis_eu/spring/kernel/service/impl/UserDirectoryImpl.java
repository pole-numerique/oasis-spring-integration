package org.oasis_eu.spring.kernel.service.impl;

import org.oasis_eu.spring.kernel.exception.TechnicalErrorException;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.model.directory.OrgMembership;
import org.oasis_eu.spring.kernel.model.directory.UserMembership;
import org.oasis_eu.spring.kernel.security.OpenIdCConfiguration;
import org.oasis_eu.spring.kernel.service.Kernel;
import org.oasis_eu.spring.kernel.service.UserDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;

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
    private Kernel kernel;


    @Override
    public List<UserMembership> getMembershipsOfUser(String userId) {



        String uriString = UriComponentsBuilder.fromHttpUrl(userDirectoryEndpoint)
                .path("/memberships/user/{user_id}")
                .queryParam("start", "0")
                .queryParam("limit", "20")
                .buildAndExpand(userId)
                .toUriString();

        ResponseEntity<UserMembership[]> response = kernel.exchange(uriString, HttpMethod.GET, null, UserMembership[].class, user());
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

        ResponseEntity<OrgMembership[]> response = kernel.exchange(userDirectoryEndpoint + "/memberships/org/{organization_id}", HttpMethod.GET, null, OrgMembership[].class, user(), organizationId);
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
