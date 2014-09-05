package org.oasis_eu.spring.kernel.model;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data holder for the info returned by the OASIS kernel's userinfo endpoint.
 * Based on: http://openid.net/specs/openid-connect-basic-1_0-28.html#StandardClaims
 *
 * User: jdenanot
 * Date: 8/29/14
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseUserInfo implements Serializable {

    @JsonProperty("name")
    private String name; // full name
    @JsonProperty("given_name")
    @NotNull @Size(min=2, max=30) //, message=" {my.profile.personal.firstname.size}")
    private String givenName; // first name
    @JsonProperty("family_name")
    @NotNull @Size(min=2, max=30) //, message="{my.profile.personal.lastname.size}")
    private String familyName; // last name
    @JsonProperty("gender")
    private String gender; // "male" or "female"
    @JsonProperty("phone_number")
    @NotNull @Size(min=7, max=15) //, message="{my.profile.personal.phoneNumber.size}")
    private String phoneNumber;
    @JsonProperty("phone_number_verified")
    private Boolean phoneNumberVerified;
    @JsonProperty("birthdate")
    private LocalDate birthdate;
    @JsonProperty("picture")
    private String pictureUrl;

    @JsonProperty("address")
    private Address address;

    @JsonProperty("updated_at")
    private Long updatedAt;


    private String locale;
    @JsonProperty("zoneinfo")
    private String zoneInfo;

    public abstract String getUserId();

    public abstract void setUserId(String userId);
    
    public abstract String getEmail();

    public abstract void setEmail(String email);
    
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

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean isPhoneNumberVerified() {
        return phoneNumberVerified;
    }

    public void setPhoneNumberVerified(boolean phoneNumberVerified) {
        this.phoneNumberVerified = phoneNumberVerified;
    }
    
    public String getLocale() {
        return locale;
	}

    @JsonProperty("locale")
    public void setLocale(String locale) {
		this.locale = locale != null ? locale.replace("-", "_") : null;
	}
    
    public String getZoneInfo() {
		return zoneInfo;
	}

	public void setZoneInfo(String zoneInfo) {
		this.zoneInfo = zoneInfo;
	}

	public String getPictureUrl() {
		return pictureUrl;
	}
    
    public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}
    
}
