package org.oasis_eu.spring.kernel.model;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data holder for the info returned by the OASIS kernel's userinfo endpoint.
 * Based on: http://openid.net/specs/openid-connect-basic-1_0-28.html#StandardClaims
 *
 * User: schambon
 * Date: 2/12/14
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfo extends BaseUserInfo implements Serializable {

    @JsonProperty("sub")
    private String userId;

	@JsonProperty("organization_id")
    private String organizationId;
    @JsonProperty("organization_admin")
    private Boolean organizationAdmin;
    @JsonProperty("email")
    private String email;
    @JsonProperty("email_verified")
    private Boolean emailVerified;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    @Deprecated
    public String getOrganizationId() {
        return organizationId;
    }

    @Deprecated
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    @Deprecated
    public Boolean isOrganizationAdmin() {
        return organizationAdmin;
    }

    @Deprecated
    public void setOrganizationAdmin(Boolean organizationAdmin) {
        this.organizationAdmin = organizationAdmin;
    }
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public Boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    public String getStreetAddress() {
        return this.getAddress() != null ? this.getAddress().getStreetAddress() : null;
    }

    public String getLocality() {
        return this.getAddress() != null ? this.getAddress().getLocality() : null;
    }

    public String getRegion() {
        return this.getAddress() != null ? this.getAddress().getRegion():null;
    }

    public String getPostalCode() {
        return this.getAddress()!=null ? this.getAddress().getPostalCode() : null;
    }
    public String getCountry() {
        return this.getAddress() != null ? this.getAddress().getCountry() : null;
    }
    
    public Instant getUpdateInstant() {
        return this.getUpdatedAt()!= null ? Instant.ofEpochSecond(this.getUpdatedAt()) : null;
    }
    
}
