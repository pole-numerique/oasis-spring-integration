package org.oasis_eu.spring.kernel.model.directory;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User: schambon
 * Date: 6/4/14
 */
public class AgentAddress {
	
    @JsonProperty("street_address")
    private String streetAddress; // ex Gustavslundsvagen 139
    @JsonProperty("locality")
    private String locality; // ex BROMMA
    @JsonProperty("region")
    private String region; // ex Stockholms Lan
    @JsonProperty("postal_code")
    private String postalCode; // ex S-167 51
    @JsonProperty("country")
    private String country;

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
