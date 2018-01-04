package org.oasis_eu.spring.kernel.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.Instant;

/**
 * Data holder for the info returned by the Ozwillo kernel's userinfo endpoint.
 * Based on: http://openid.net/specs/openid-connect-basic-1_0-28.html#StandardClaims
 *
 * User: schambon
 * Date: 2/12/14
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfo extends BaseUserInfo implements Serializable {
    private static final long serialVersionUID = 5630983084892826427L;

    @JsonProperty("sub")
    private String userId;
    @JsonProperty("email")
    private String email;
    @JsonProperty("email_verified")
    private Boolean emailVerified;

    public static UserInfo from(UserInfo in) {
        UserInfo out = new UserInfo();
        out.setName(in.getName());
        out.setNickname(in.getNickname());
        out.setLocale(in.getLocale());
        out.setEmail(in.getEmail());
        out.setEmailVerified(in.isEmailVerified());
        out.setUserId(in.getUserId());
        out.setAddress(in.getAddress()); // mmm, it'd be better to deepclone, but let's not worry too much right now (we'll fix it when we see weird bugs)
        out.setBirthdate(in.getBirthdate());
        out.setFamilyName(in.getFamilyName());
        out.setGivenName(in.getGivenName());
        out.setGender(in.getGender());
        out.setPhoneNumber(in.getPhoneNumber());
        out.setPhoneNumberVerified(in.isPhoneNumberVerified() != null ? in.isPhoneNumberVerified() : false);
        out.setPictureUrl(in.getPictureUrl());
        out.setUpdatedAt(in.getUpdatedAt());
        out.setZoneInfo(in.getZoneInfo());

        return out;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }


    @Override
    public String getEmail() {
        return email;
    }

    @Override
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
