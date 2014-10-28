package org.oasis_eu.spring.kernel.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
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
import org.springframework.cache.annotation.CacheEvict;
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
    public List<OrgMembership> getMembershipsOfOrganization(String organizationId) {

        return getMembershipsOfOrganization(organizationId, -1, -1);
    }

    @Override
    @Cacheable(value = "org-memberships", key = "#organizationId")
    public List<OrgMembership> getMembershipsOfOrganization(String organizationId, int start, int limit) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(userDirectoryEndpoint)
                .path("/memberships/org/{organization_id}");

        if (start != -1) {
            builder = builder.queryParam("start", start);
        }
        if (limit != -1) {
            builder = builder.queryParam("limit", limit);
        }

        String uri = builder.buildAndExpand(organizationId).toUriString();

        ResponseEntity<OrgMembership[]> response = kernel.exchange(uri, HttpMethod.GET, null, OrgMembership[].class, user());
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
    @CacheEvict(value = "accounts", key = "#userAccount.userId")
    public void saveUserAccount(UserAccount userAccount) {

        ResponseEntity<UserAccount> entity = kernel.exchange(userDirectoryEndpoint + "/user/{userId}", HttpMethod.GET, null, UserAccount.class, user(), userAccount.getUserId());
        String etag = entity.getHeaders().getETag();

        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", etag);
        headers.setContentType(MediaType.APPLICATION_JSON);

        kernel.exchange(userDirectoryEndpoint + "/user/{userId}", HttpMethod.PUT, new HttpEntity<Object>(userAccount, headers), UserAccount.class, user(), userAccount.getUserId());

    }

    @Override
    @Cacheable("accounts")
    public UserAccount findUserAccount(String id) {

        ResponseEntity<UserAccount> entity = kernel.exchange(userDirectoryEndpoint + "/user/{userId}", HttpMethod.GET, null, UserAccount.class, user(), id);
        if (entity.getStatusCode().is2xxSuccessful()) {

            return entity.getBody();
        } else {
            logger.error("Cannot load user account {}, status is: {}", id, entity.getStatusCode());
            throw new WrongQueryException();
        }
    }

    private void updateMembership(String membershipUri, String membershipEtag, boolean admin) {
        class MembershipRequest {
            @JsonProperty boolean admin;
        }

        MembershipRequest r = new MembershipRequest();
        r.admin = admin;

        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", membershipEtag);
        HttpEntity<MembershipRequest> request = new HttpEntity<>(r, headers);
        kernel.exchange(membershipUri, HttpMethod.PUT, request, Void.class, user());
    }

    @Override
    @CacheEvict(value = "user-memberships", key = "#userId")
    public void updateMembership(UserMembership um, boolean admin, String userId) {
        updateMembership(um.getMembershipUri(), um.getMembershipEtag(), admin);
    }

    @Override
    @CacheEvict(value = "org-memberships", key = "#organizationId")
    public void updateMembership(OrgMembership om, boolean admin, String organizationId) {
        updateMembership(om.getMembershipUri(), om.getMembershipEtag(), admin);
    }

    @Override
    @CacheEvict(value = "org-memberships", key = "#organizationId")
    public void removeMembership(OrgMembership orgMembership, String organizationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", orgMembership.getMembershipEtag());

        kernel.exchange(orgMembership.getMembershipUri(), HttpMethod.DELETE, new HttpEntity<>(headers), Void.class, user());
    }

    @Override
    @CacheEvict(value = "user-memberships", key = "#userId")
    public void removeMembership(UserMembership userMembership, String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", userMembership.getMembershipEtag());
        kernel.exchange(userMembership.getMembershipUri(), HttpMethod.DELETE, new HttpEntity<Object>(headers), Void.class, user());
    }

    @Override
    public void createMembership(String email, String organizationId) {
        class MembershipRequest {
            @JsonProperty String email;
            @JsonProperty boolean admin = false;
        }

        String uriString = UriComponentsBuilder.fromHttpUrl(userDirectoryEndpoint)
                .path("/memberships/org/{organization_id}")
                .build()
                .expand(organizationId)
                .toUriString();


        MembershipRequest request = new MembershipRequest();
        request.email = email;

        kernel.exchange(uriString, HttpMethod.POST, new HttpEntity<Object>(request), Void.class, user());
    }
}
