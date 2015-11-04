package org.oasis_eu.spring.kernel.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.stream.Collectors;

/**
 * Data holder for the info returned by the Ozwillo kernel's userinfo endpoint.
 * Based on: http://openid.net/specs/openid-connect-basic-1_0-28.html#StandardClaims
 * 
 * Also provides user locale negociation helpers
 *
 * User: jdenanot
 * Date: 8/29/14
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseUserInfo implements Serializable {
    private static final long serialVersionUID = -5272426743544132097L;

    @JsonProperty("name")
    private String name; // full name
    @JsonProperty("given_name") // TODO upgrade : middle_name
    private String givenName; // first name
    @JsonProperty("family_name")
    private String familyName; // last name
    @JsonProperty("gender")
    private String gender; // "male" or "female"

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("phone_number")
    private String phoneNumber;
    @JsonProperty("phone_number_verified")
    private Boolean phoneNumberVerified;
    @JsonProperty("birthdate")
    private LocalDate birthdate;
    @JsonProperty("picture")
    private String pictureUrl;

    @JsonProperty("address")
    private Address address;

    @JsonProperty("updated_at") // TODO upgrade : created_at
    private Long updatedAt;

    /** OpenID Connect's ui_locales ex. "en-GB fr", to be parsed by get*Locale*() methods
     * or further by UserInfoService methods
     * (or by Locale methods, ex. forLanguageTag() to get the first one) */
    @JsonProperty("locale")
    private String locale;
    @JsonProperty("zoneinfo")
    private String zoneInfo;

    @JsonProperty("nickname")
    private String nickname;

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

    public String getPhone() {
        return phoneNumber;
    }

    public void setPhone(String phone) {
        setPhoneNumber(phone);
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
    
    /**
     * 
     * @return locale ex. "en-GB fr"parsed as Locales
     */
    public List<Locale> getLocales() {
        if (locale == null) {
            return null;
        }
        return Arrays.stream(locale.split(" ")) // NB. uses fastpath when single char pattern http://stackoverflow.com/questions/5965767/performance-of-stringtokenizer-class-vs-split-method-in-java
            .map(localeString -> Locale.forLanguageTag(localeString))
            .collect(Collectors.toList());
    }
    
    /**
     * shortcut ; ideally rather use getBestLocale()
     * @return
     */
    public Locale getFirstLocale() {
        if (locale == null) {
            return null;
        }
        return getLocales().get(0);
    }
    
    /**
     * Negociates the best locale for the user among the given available ones
     * @param localeList
     * @return null if null, "und" Locale if none, best otherwise
     */
    public Locale getBestLocale(List<Locale> localeList) {
        if (locale == null) {
            return null;
        }
        return undefinedLocaleIfNull(Locale.lookup(getLocaleAsLanguageRanges(), localeList));
    }
    
    public static Locale undefinedLocaleIfNull(Locale locale) {
        return (locale != null) ? locale : Locale.forLanguageTag("und");
    }

    /**
     * Negociates the best language tag for the user among the given available ones
     * (same as getBestLocale)
     * @param languageTagList
     * @return
     */
    public String getBestLanguageTag(List<String> languageTagList) {
        if (locale == null) {
            return null;
        }
        return Locale.lookupTag(getLocaleAsLanguageRanges(), languageTagList);
    }
    
    /**
     * Allows to do what getBestLocale/LanguageTag does using Locale.lookup(Tag)()
     * @return locale parsed as LanguageRanges, locale being ex. "fr-FR en-GB es"
     * (actually "fr-FR,en-GB", "en-US;q=1.0,en-GB;q=0.5,fr-FR;q=0.0" also works)
     */
    public List<LanguageRange> getLocaleAsLanguageRanges() {
        if (locale == null) {
            return null;
        }
        return Locale.LanguageRange.parse(locale); // works with "fr-FR en-GB"
        // (actually also with "fr-FR,en-GB", "en-US;q=1.0,en-GB;q=0.5,fr-FR;q=0.0" http://docs.oracle.com/javase/tutorial/i18n/locale/matching.html
    }
    
    public String getLocale() {
        return locale;
	}

    public void setLocale(String locale) {
        this.locale = locale;
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
    
    /*
     * computes ${modelObject.__${widget.id}__} before applying if rendering condition,
     * then would fail with password field not found.
     * <div class="col-sm-10" data-th-if="${'text'.equals(widget.type)}"
                data-th-include="includes/my-profile-fragments :: text-widget (${widget.id}, ${modelObject.__${widget.id}__}, ${layout.mode})">?</div>
     * http://www.captaindebug.com/2012/01/autowiring-using-value-and-optional.html#.VBG9QtbAOR8
     * http://stackoverflow.com/questions/20431344/is-it-possible-to-make-spring-ignore-a-bean-property-that-is-not-writable-or-has
     * http://stackoverflow.com/questions/11773122/how-to-define-not-mandatory-property-in-spring
     */
    public String getPassword() {
    	
    	return null;
    }


    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
