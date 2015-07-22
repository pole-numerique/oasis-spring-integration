package org.oasis_eu.spring.kernel.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * User: schambon
 * Date: 4/23/14
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Address implements Serializable {
    private static final long serialVersionUID = 4187734521814253785L;

    @JsonProperty("street_address")
    private String streetAddress; // ex Gustavslundsvagen 139
    private String locality; // ex BROMMA
    private String region; // ex Stockholms Lan

    @JsonProperty("postal_code")
    private String postalCode; // ex S-167 51
    private String country;

    public Address(){}

    public Address(String streetAddress,String locality,String region,String postalCode,String country){
        this.streetAddress = streetAddress;
        this.locality = locality;
        this.region = region;
        this.postalCode = postalCode;
        this.country = country;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
