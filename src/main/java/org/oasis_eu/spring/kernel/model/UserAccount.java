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
 * User: jdenanot
 * Date: 8/29/14
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAccount extends BaseUserInfo implements Serializable {
    
    @JsonProperty("id")
    private String userId;
    
    @JsonProperty("email_address")
    private String email;
    
    public UserAccount() {
    	
    	super();
    }
    
    public UserAccount(UserInfo userInfo) {
    	
    	super();
    	this.userId = userInfo.getUserId();
    	this.email = userInfo.getEmail();
    	this.setAddress(userInfo.getAddress());
    	this.setBirthdate(userInfo.getBirthdate());
    	this.setFamilyName(userInfo.getFamilyName());
    	this.setGender(userInfo.getGender());
    	this.setGivenName(userInfo.getGivenName());
    	this.setLocale(userInfo.getLocale());
    	this.setName(userInfo.getName());
    	this.setPhoneNumber(userInfo.getPhoneNumber());
    	if(userInfo.isPhoneNumberVerified()!=null) {
    		this.setPhoneNumberVerified(userInfo.isPhoneNumberVerified());
    	}
    	this.setPictureUrl(userInfo.getPictureUrl());
    	this.setUpdatedAt(userInfo.getUpdatedAt());
    	this.setZoneInfo(userInfo.getZoneInfo());
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
}
