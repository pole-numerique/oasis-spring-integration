package org.oasis_eu.spring.kernel.model.instance;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

/**
 * User: schambon
 * Date: 7/1/14
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceCreated {

    @JsonProperty("id")
    private String id;

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

    @JsonProperty("post_logout_redirect_uris")
    private List<String> postLogoutRedirectUris;

    @JsonProperty("target_audience")
    private List<Audience> targetAudience;

    @JsonProperty("geographical_areas")
    private List<String> geographicalAreas;

    @JsonProperty("provider_id")
    private String providerId;

    @JsonProperty("tos_uri")
    private String defaultTosUri;

    @JsonProperty("policy_uri")
    private String defaultPolicyUri;

    private Map<String, String> localizedTosUris = new HashMap<>();

    private Map<String, String> localizedPolicyUris = new HashMap<>();

    @JsonProperty("screenshot_uris")
    private List<String> screenshotUris = new ArrayList<>();

    @JsonProperty("contacts")
    private List<String> contacts = new ArrayList<>();

    @JsonProperty("subscription_uri")
    private String subscriptionUri;

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
        } else if (key.startsWith("tos_uri#")) {
            localizedTosUris.put(key.substring("tos_uri#".length()), value);
        } else if (key.startsWith("policy_uri#")) {
            localizedPolicyUris.put(key.substring("policy_uri#".length()), value);
        }

    }

    @JsonAnyGetter
    public Map<String, String> getTranslations() {

        Map<String, String> result = new HashMap<>();
        localizedNames.entrySet().forEach(e -> result.put("name#" + e.getKey(), e.getValue()));
        localizedDescriptions.entrySet().forEach(e -> result.put("description#" + e.getKey(), e.getValue()));
        localizedIcons.entrySet().forEach(e -> result.put("icon#" + e.getKey(), e.getValue()));
        localizedTosUris.entrySet().forEach(e -> result.put("tos_uri#" + e.getKey(), e.getValue()));
        localizedPolicyUris.entrySet().forEach(e -> result.put("policy_uri#" + e.getKey(), e.getValue()));

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

    public List<Audience> getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(List<Audience> targetAudience) {
        this.targetAudience = targetAudience;
    }

    public List<String> getGeographicalAreas() {
        return geographicalAreas;
    }

    public void setGeographicalAreas(List<String> geographicalAreas) {
        this.geographicalAreas = geographicalAreas;
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

    public List<String> getPostLogoutRedirectUris() {
        return postLogoutRedirectUris;
    }

    public void setPostLogoutRedirectUris(List<String> postLogoutRedirectUris) {
        this.postLogoutRedirectUris = postLogoutRedirectUris;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDefaultTosUri() {
        return defaultTosUri;
    }

    public void setDefaultTosUri(String defaultTosUri) {
        this.defaultTosUri = defaultTosUri;
    }

    public List<String> getScreenshotUris() {
        return screenshotUris;
    }

    public void setScreenshotUris(List<String> screenshotUris) {
        this.screenshotUris = screenshotUris;
    }

    public List<String> getContacts() {
        return contacts;
    }

    public void setContacts(List<String> contacts) {
        this.contacts = contacts;
    }

    public String getDefaultPolicyUri() {
        return defaultPolicyUri;
    }

    public void setDefaultPolicyUri(String defaultPolicyUri) {
        this.defaultPolicyUri = defaultPolicyUri;
    }

    public String getSubscriptionUri() {
        return subscriptionUri;
    }

    public void setSubscriptionUri(String subscriptionUri) {
        this.subscriptionUri = subscriptionUri;
    }

    public void setLocalizedTosUris(Map<String, String> localizedTosUris) {
        this.localizedTosUris = localizedTosUris;
    }

    public void setLocalizedPolicyUris(Map<String, String> localizedPolicyUris) {
        this.localizedPolicyUris = localizedPolicyUris;
    }

    @Override
    public String toString() {
        return "ServiceCreated{" +
                "id='" + id + '\'' +
                ", identifier='" + identifier + '\'' +
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
                ", postLogoutRedirectUris=" + postLogoutRedirectUris +
                ", targetAudience=" + targetAudience +
                ", geographicalAreas='" + geographicalAreas + '\'' +
                ", providerId='" + providerId + '\'' +
                '}';
    }
}
