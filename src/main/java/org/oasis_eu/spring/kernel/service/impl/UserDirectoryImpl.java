package org.oasis_eu.spring.kernel.service.impl;

import org.oasis_eu.spring.kernel.exception.TechnicalErrorException;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.model.UserAccount;
import org.oasis_eu.spring.kernel.model.directory.OrgMembership;
import org.oasis_eu.spring.kernel.model.directory.UserMembership;
import org.oasis_eu.spring.kernel.service.Kernel;
import org.oasis_eu.spring.kernel.service.UserDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;

@Repository
public class UserDirectoryImpl implements UserDirectory {

    private static final Logger logger = LoggerFactory.getLogger(UserDirectoryImpl.class);

    @Value("${kernel.user_directory_endpoint}")
    private String userDirectoryEndpoint;

    @Autowired
    private Kernel kernel;


    @Override
    @Cacheable("user-memberships")
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
            logger.debug("Found memberships in the following organizations:\n{}", orgs);

            return Arrays.asList(response.getBody());
        } else {
            logger.error("Cannot load user membership information: {}", response.getStatusCode());

            if (response.getStatusCode().is4xxClientError()) {
                throw new WrongQueryException();
            } else {
                throw new TechnicalErrorException();
            }
        }


    }

    @Override
    @Cacheable("org-memberships")
    public List<OrgMembership> getMembershipsOfOrganization(String organizationId) {

        ResponseEntity<OrgMembership[]> response = kernel.exchange(userDirectoryEndpoint + "/memberships/org/{organization_id}", HttpMethod.GET, null, OrgMembership[].class, user(), organizationId);
        if (response.getStatusCode().is2xxSuccessful()) {
            return Arrays.asList(response.getBody());
        } else {
            logger.error("Cannot load organization memberships: {}", response.getStatusCode());
            if (response.getStatusCode().is4xxClientError()) {
                throw new WrongQueryException();
            } else {
                throw new TechnicalErrorException();
            }
        }
    }


    @Override
    public void saveUserAccount(UserAccount userAccount) {

        ResponseEntity<UserAccount> entity = kernel.exchange(userDirectoryEndpoint + "/user/{userId}", HttpMethod.GET, null, UserAccount.class, user(), userAccount.getUserId());
        String etag = entity.getHeaders().getETag();

        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", etag);
        headers.setContentType(MediaType.APPLICATION_JSON);

        kernel.exchange(userDirectoryEndpoint + "/user/{userId}", HttpMethod.PUT, new HttpEntity<Object>(userAccount, headers), UserAccount.class, user(), userAccount.getUserId());

    }

    @Override
    public UserAccount findUserAccount(String id) {

        ResponseEntity<UserAccount> entity = kernel.exchange(userDirectoryEndpoint + "/user/{userId}", HttpMethod.GET, null, UserAccount.class, user(), id);
        if (entity.getStatusCode().is2xxSuccessful()) {

            return entity.getBody();
        } else {
            logger.error("Cannot load user account {}, status is: {}", id, entity.getStatusCode());
            throw new WrongQueryException();
        }
    }
}
