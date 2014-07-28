package org.oasis_eu.spring.kernel.model.instance;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 7/23/14
 */
public class ScopeNeeded {

    @JsonProperty("scope_id")
    private String scopeId;

    @JsonProperty("motivation")
    private String motivation;

    @JsonIgnore
    private Map<String, String> localizedMotivations;

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    public Map<String, String> getLocalizedMotivations() {
        return localizedMotivations;
    }

    @JsonIgnore
    public void setLocalizedMotivations(Map<String, String> localizedMotivations) {
        this.localizedMotivations = localizedMotivations;
    }

    @JsonAnySetter
    public void setTranslation(String key, String value) {
        if (key.startsWith("motivation#")) {
            localizedMotivations.put(key.substring("motivation#".length()), value);
        } else {
            throw new IllegalArgumentException(String.format("Cannot match key %s to properties", key));
        }
    }

    @JsonAnyGetter
    public Map<String, String> getTranslations() {
        return localizedMotivations.entrySet().stream().collect(Collectors.toMap(e -> "motivation#" + e.getKey(), Map.Entry::getValue));
    }

    @Override
    public String toString() {
        return "ScopeNeeded{" +
                "scopeId='" + scopeId + '\'' +
                ", motivation='" + motivation + '\'' +
                ", localizedMotivations=" + localizedMotivations +
                '}';
    }
}
