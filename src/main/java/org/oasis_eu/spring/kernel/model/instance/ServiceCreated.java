package org.oasis_eu.spring.kernel.model.instance;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * User: schambon
 * Date: 7/1/14
 */
public class ServiceCreated {

    @JsonProperty("local_id")
    private String identifier;

    @JsonProperty("service_uri")
    private String url;

    @JsonProperty("visible")
    private boolean visible = false;

    @JsonProperty("name")
    private String defaultName;

    @JsonProperty("description")
    private String defaultDescription;

    @JsonProperty("icon")
    private String defaultIcon;

    private Map<String, String> localizedNames = new HashMap<>();

    private Map<String, String> localizedDescriptions = new HashMap<>();

    private Map<String, String> localizedIcons = new HashMap<>();

    @JsonProperty("notification_uri")
    private String notificationUrl;

    @JsonProperty("category_ids")
    private List<String> categoryIds = new ArrayList<>();

    @JsonProperty("payment_option")
    private PaymentOption paymentOption;

    @JsonProperty("redirect_uris")
    private List<String> redirectUris;

    @JsonProperty("target_audience")
    private Audience targetAudience;

    @JsonProperty("territory_id")
    private String territoryId;

    @JsonProperty("provider_id")
    private String providerId;



    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }



    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    @JsonIgnore
    public void setLocalizedNames(Map<String, String> localizedNames) {
        this.localizedNames = localizedNames;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    public void setDefaultDescription(String defaultDescription) {
        this.defaultDescription = defaultDescription;
    }


    @JsonIgnore
    public void setLocalizedDescriptions(Map<String, String> localizedDescriptions) {
        this.localizedDescriptions = localizedDescriptions;
    }

    @JsonIgnore
    public void setLocalizedIcons(Map<String, String> localizedIcons) {
        this.localizedIcons = localizedIcons;
    }


    @JsonAnySetter
    public void setTranslation(String key, String value) {

        if (key.startsWith("name#")) {
            localizedNames.put(key.substring("name#".length()), value);
        } else if (key.startsWith("description#")) {
            localizedDescriptions.put(key.substring("description#".length()), value);
        } else if (key.startsWith("icon#")) {
            localizedIcons.put(key.substring("icon#".length()), value);
        }

    }

    @JsonAnyGetter
    public Map<String, String> getTranslations() {

        Map<String, String> result = new HashMap<>();
        localizedNames.entrySet().forEach(e -> result.put("name#" + e.getKey(), e.getValue()));
        localizedDescriptions.entrySet().forEach(e -> result.put("description#" + e.getKey(), e.getValue()));
        localizedIcons.entrySet().forEach(e -> result.put("icon#" + e.getKey(), e.getValue()));

        return result;
    }

    public String getDefaultIcon() {
        return defaultIcon;
    }

    public void setDefaultIcon(String defaultIcon) {
        this.defaultIcon = defaultIcon;
    }

    public List<String> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<String> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    public PaymentOption getPaymentOption() {
        return paymentOption;
    }

    public void setPaymentOption(PaymentOption paymentOption) {
        this.paymentOption = paymentOption;
    }

    public Audience getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(Audience targetAudience) {
        this.targetAudience = targetAudience;
    }

    public String getTerritoryId() {
        return territoryId;
    }

    public void setTerritoryId(String territoryId) {
        this.territoryId = territoryId;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getName(Locale locale) {
        if (localizedNames.containsKey(locale.getLanguage())) {
            return localizedNames.get(locale.getLanguage());
        } else {
            return defaultName;
        }
    }

    public String getDescription(Locale locale) {
        if (localizedDescriptions.containsKey(locale.getLanguage())) {
            return localizedDescriptions.get(locale.getLanguage());
        } else {
            return defaultDescription;
        }
    }

    public String getIcon(Locale locale) {
        if (localizedIcons.containsKey(locale.getLanguage())) {
            return localizedIcons.get(locale.getLanguage());
        } else {
            return defaultIcon;
        }
    }

    public List<String> getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(List<String> redirectUris) {
        this.redirectUris = redirectUris;
    }

    @Override
    public String toString() {
        return "ServiceCreated{" +
                "identifier='" + identifier + '\'' +
                ", url='" + url + '\'' +
                ", visible=" + visible +
                ", defaultName='" + defaultName + '\'' +
                ", defaultDescription='" + defaultDescription + '\'' +
                ", defaultIcon='" + defaultIcon + '\'' +
                ", localizedNames=" + localizedNames +
                ", localizedDescriptions=" + localizedDescriptions +
                ", localizedIcons=" + localizedIcons +
                ", notificationUrl='" + notificationUrl + '\'' +
                ", categoryIds=" + categoryIds +
                ", paymentOption=" + paymentOption +
                ", redirectUris=" + redirectUris +
                ", targetAudience=" + targetAudience +
                ", territoryId='" + territoryId + '\'' +
                ", providerId='" + providerId + '\'' +
                '}';
    }
}
