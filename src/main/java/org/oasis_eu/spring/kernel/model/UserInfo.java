package org.oasis_eu.spring.kernel.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Data holder for the info returned by the OASIS kernel's userinfo endpoint.
 * Based on: http://openid.net/specs/openid-connect-basic-1_0-28.html#StandardClaims
 *
 * User: schambon
 * Date: 2/12/14
 */
public class UserInfo {

    @JsonProperty("sub")
    private String userId;

    @JsonProperty("organization_id")
    private String organizationId;
    @JsonProperty("organization_admin")
    private boolean organizationAdmin;
    @JsonProperty("name")
    private String name; // full name
    @JsonProperty("given_name")
    private String givenName; // first name
    @JsonProperty("family_name")
    private String familyName; // last name
    @JsonProperty("gender")
    private String gender; // "male" or "female"
    @JsonProperty("email")
    private String email;
    @JsonProperty("email_verified")
    private boolean emailVerified;
    @JsonProperty("phone_number")
    private String phoneNumber;
    @JsonProperty("phone_number_verified")
    private boolean phoneNumberVerified;
    @JsonProperty("birthdate")
    private LocalDate birthdate;

    @JsonProperty("address")
    private Address address;

    @JsonProperty("updated_at")
    private long updatedAt;


    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public boolean isOrganizationAdmin() {
        return organizationAdmin;
    }

    public void setOrganizationAdmin(boolean organizationAdmin) {
        this.organizationAdmin = organizationAdmin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String lastName) {
        this.familyName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getStreetAddress() {
        return address != null ? address.getStreetAddress() : null;
    }

    public String getLocality() {
        return address != null ? address.getLocality() : null;
    }

    public String getRegion() {
        return address != null ? address.getRegion():null;
    }

    public String getPostalCode() {
        return address!=null ? address.getPostalCode() : null;
    }
    public String getCountry() {
        return address != null ? address.getCountry() : null;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getUpdateInstant() {
        return Instant.ofEpochSecond(updatedAt);
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean isPhoneNumberVerified() {
        return phoneNumberVerified;
    }

    public void setPhoneNumberVerified(boolean phoneNumberVerified) {
        this.phoneNumberVerified = phoneNumberVerified;
    }
}
