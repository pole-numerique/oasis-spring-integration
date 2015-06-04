package org.oasis_eu.spring.kernel.service.impl;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;

import java.util.Arrays;
import java.util.List;

import org.oasis_eu.spring.kernel.exception.EntityNotFoundException;
import org.oasis_eu.spring.kernel.exception.ForbiddenException;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.model.UserAccount;
import org.oasis_eu.spring.kernel.model.directory.OrgMembership;
import org.oasis_eu.spring.kernel.model.directory.PendingOrgMembership;
import org.oasis_eu.spring.kernel.model.directory.UserMembership;
import org.oasis_eu.spring.kernel.service.Kernel;
import org.oasis_eu.spring.kernel.service.UserDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

@Repository
public class UserDirectoryImpl implements UserDirectory {

    private static final Logger logger = LoggerFactory.getLogger(UserDirectoryImpl.class);

    @Value("${kernel.user_directory_endpoint}")
    private String userDirectoryEndpoint;

    @Autowired private Kernel kernel;

    @Autowired private MessageSource messageSource;

    @Override
    @Cacheable("user-memberships")
    public List<UserMembership> getMembershipsOfUser(String userId) throws WrongQueryException {

        String uriString = UriComponentsBuilder.fromHttpUrl(userDirectoryEndpoint)
                .path("/memberships/user/{user_id}")
                .queryParam("start", "0")
                .queryParam("limit", "20")
                .buildAndExpand(userId)
                .toUriString();

        UserMembership[] userMembpArray = kernel.getEntityOrException(uriString, UserMembership[].class, user());

        StringBuilder orgs = new StringBuilder();
        for (UserMembership m : userMembpArray ) {
            orgs.append( String.format("%s (%s)\n", m.getOrganizationName(), m.getOrganizationId()) );
        }
        logger.debug("Found memberships in the following organizations:\n{}", orgs);

        return Arrays.asList(userMembpArray);
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

        OrgMembership[] response = kernel.getEntityOrNull(uri, OrgMembership[].class, user());
        return Arrays.asList(response);
    }

    @Override
    public List<OrgMembership> getAdminsOfOrganization(String organizationId) {
        String uri = UriComponentsBuilder.fromHttpUrl(userDirectoryEndpoint)
                .path("/memberships/org/{organization_id}/admins")
                .buildAndExpand(organizationId)
                .toUriString();
        
        OrgMembership[] omsArray = kernel.getEntityOrNull(uri, OrgMembership[].class, user());
        return Arrays.asList(omsArray);
    }

    @Override
    @CacheEvict(value = "accounts", key = "#userAccount.userId")
    public void saveUserAccount(UserAccount userAccount) throws WrongQueryException {

    	ResponseEntity<UserAccount> response = kernel.exchange(userDirectoryEndpoint + "/user/{userId}",
    			HttpMethod.GET, null, UserAccount.class, user(), userAccount.getUserId());

        String etag = response.getHeaders().getETag();

        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", etag);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String uriString = userDirectoryEndpoint + "/user/{userId}";
        ResponseEntity<UserAccount> kernelResp = kernel.exchange(uriString, HttpMethod.PUT, new HttpEntity<Object>(userAccount, headers),
        		UserAccount.class, user(), userAccount.getUserId());
        // validate response body
        kernel.getBodyUnlessClientError(kernelResp, UserAccount.class, uriString); // TODO test
    }

    @Override
    @Cacheable("accounts")
    public UserAccount findUserAccount(String id) {
    	return kernel.getEntityOrException(userDirectoryEndpoint + "/user/{userId}", UserAccount.class, user(), id);
    }


    /**
     * TODO remove. its never used
     * @param um
     * @param admin
     * @param userId
     */
    @Override
    @CacheEvict(value = "user-memberships", key = "#userId")
    public void updateMembership(UserMembership um, boolean admin, String userId) throws WrongQueryException {
        updateMembership(um.getMembershipUri(), um.getMembershipEtag(), admin);
    }

    @Override
    @CacheEvict(value = "org-memberships", key = "#organizationId")
    public void updateMembership(OrgMembership om, boolean admin, String organizationId) throws WrongQueryException {
        updateMembership(om.getMembershipUri(), om.getMembershipEtag(), admin);
    }

    private void updateMembership(String membershipUri, String membershipEtag, boolean admin) throws WrongQueryException {
        class MembershipRequest {
            @JsonProperty boolean admin;
        }

        MembershipRequest request = new MembershipRequest();
        request.admin = admin;

        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", membershipEtag);

        ResponseEntity<Void> kernelResp = kernel.exchange(membershipUri, HttpMethod.PUT, new HttpEntity<>(request, headers), Void.class, user());
        // validate response body
        kernel.getBodyUnlessClientError(kernelResp, Void.class, membershipUri);
    }

    @Override
    @CacheEvict(value = "org-memberships", key = "#organizationId")
    public void removeMembership(OrgMembership orgMembership, String organizationId) throws WrongQueryException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", orgMembership.getMembershipEtag());

        String uriString = orgMembership.getMembershipUri();
        ResponseEntity<Void> kernelResp = kernel.exchange(uriString, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class, user());
        // validate response body
        kernel.getBodyUnlessClientError(kernelResp, Void.class, uriString); // TODO test
    }

    @Override
    @CacheEvict(value = "user-memberships", key = "#userId")
    public void removeMembership(UserMembership userMembership, String userId) throws WrongQueryException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", userMembership.getMembershipEtag());
        String uriString = userMembership.getMembershipUri();
        ResponseEntity<Void> kernelResp = kernel.exchange(uriString, HttpMethod.DELETE, new HttpEntity<Object>(headers), Void.class, user());
        // validate response body
        kernel.getBodyUnlessClientError(kernelResp, Void.class, uriString ); // TODO test
    }

    @Override
    public void createMembership(String email, String organizationId) throws EntityNotFoundException,
            ForbiddenException, WrongQueryException {

        class MembershipRequest {
            @JsonProperty
            String email;
            @JsonProperty
            boolean admin = false;
        }

        String uriString = UriComponentsBuilder.fromHttpUrl(userDirectoryEndpoint)
                .path("/memberships/org/{organization_id}").build().expand(organizationId).toUriString();

        MembershipRequest request = new MembershipRequest();
        request.email = email;

        ResponseEntity<Void> kernelResp = kernel.exchange(uriString, HttpMethod.POST, new HttpEntity<Object>(request),
                Void.class, user());
        // validate response body (business message is set to a more specific
        // one in CONFLICT case in portal service)
        kernel.getBodyUnlessClientError(kernelResp, Void.class, uriString);
    }

    @Override
    @CacheEvict(value = "pending-memberships", key = "#pendingMembershipId")
    public void removePendingMembership(String pendingMembershipId, String pendingMembershipETag)
            throws EntityNotFoundException, ForbiddenException, WrongQueryException {
        String uriString = UriComponentsBuilder.fromHttpUrl(userDirectoryEndpoint)
                .path("/pending-memberships/membership/{membership_id}").build().expand(pendingMembershipId)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", pendingMembershipETag);

        ResponseEntity<Void> kernelResp = kernel.exchange(uriString, HttpMethod.DELETE,
                new HttpEntity<Object>(headers), Void.class, user());
        // validate response body
        kernel.getBodyUnlessClientError(kernelResp, Void.class, uriString);
    }

    @Override
    public List<PendingOrgMembership> getPendingOrgMembership(String organizationId) {
        return getPendingOrgMembership(organizationId, -1, -1);
    }

    @Override
    @Cacheable(value = "pending-memberships", key = "#organizationId")
    public List<PendingOrgMembership> getPendingOrgMembership(String organizationId, int start, int limit) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(userDirectoryEndpoint).path(
                "/pending-memberships/org/{organization_id}");

        if (start != -1) {
            builder = builder.queryParam("start", start);
        }
        if (limit != -1) {
            builder = builder.queryParam("limit", limit);
        }

        String uri = builder.buildAndExpand(organizationId).toUriString();

        PendingOrgMembership[] response = kernel.getEntityOrNull(uri, PendingOrgMembership[].class, user());
        return Arrays.asList(response);
    }
}
