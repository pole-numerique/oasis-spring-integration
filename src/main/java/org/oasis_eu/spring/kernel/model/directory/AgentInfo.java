package org.oasis_eu.spring.kernel.model.directory;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Mostly duplicates user info, but with slight differences
 * User: schambon
 * Date: 6/4/14
 */
public class AgentInfo {

    @JsonProperty("name")
    private String name; // full name
    @JsonProperty("family_name")
    private String familyName; // last name
    @JsonProperty("given_name")
    private String givenName; // first name
    @JsonProperty("middle_name")
    private String middleName; // middle name
    @JsonProperty("nickname")
    private String nickname; // nickname
    @JsonProperty("picture")
    private String picture; // picture?
    @JsonProperty("gender")
    private String gender; // "male" or "female"
    @JsonProperty("birthdate")
    private LocalDate birthdate;
    @JsonProperty("zoneinfo")
    private String zoneInfo; // zone info?

    @JsonProperty("locale")
    private String locale;

    @JsonProperty("updated_at")
    private long updatedAt;

    @JsonProperty("email")
    private String email;
    @JsonProperty("email_verified")
    private boolean emailVerified;

    @JsonProperty("address")
    private AgentAddress address;

    @JsonProperty("phone")
    private String phoneNumber;
    @JsonProperty("phone_verified")
    private boolean phoneNumberVerified;

    @JsonProperty("organization_id")
    private String organizationId;
    @JsonProperty("organization_admin")
    private boolean organizationAdmin;


    @JsonProperty("modified")
    private long modified;

    @JsonProperty("id")
    private String id;


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

    public AgentAddress getAddress() {
        return address;
    }

    public void setAddress(AgentAddress address) {
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

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getZoneInfo() {
        return zoneInfo;
    }

    public void setZoneInfo(String zoneInfo) {
        this.zoneInfo = zoneInfo;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
