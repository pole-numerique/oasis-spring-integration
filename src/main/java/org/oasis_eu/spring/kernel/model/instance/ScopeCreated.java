package org.oasis_eu.spring.kernel.model.instance;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * User: schambon
 * Date: 7/23/14
 */
public class ScopeCreated {

    @JsonProperty("local_id")
    private String scopeId;

    @JsonProperty("name")
    private String defaultName;

    @JsonProperty("description")
    private String defaultDescription;

    private Map<String, String> localizedNames;
    private Map<String, String> localizedDescriptions;


    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    public void setDefaultDescription(String defaultDescription) {
        this.defaultDescription = defaultDescription;
    }

    public Map<String, String> getLocalizedNames() {
        return localizedNames;
    }

    @JsonIgnore
    public void setLocalizedNames(Map<String, String> localizedNames) {
        this.localizedNames = localizedNames;
    }

    public Map<String, String> getLocalizedDescriptions() {
        return localizedDescriptions;
    }

    @JsonIgnore
    public void setLocalizedDescriptions(Map<String, String> localizedDescriptions) {
        this.localizedDescriptions = localizedDescriptions;
    }

    @JsonAnySetter
    public void setTranslation(String key, String value) {
        if (key.startsWith("name#")) {
            localizedNames.put(key.substring("name#".length()), value);
        } else if (key.startsWith("description#")) {
            localizedDescriptions.put(key.substring("description#".length()), value);
        } else {
            throw new IllegalArgumentException(String.format("Cannot match key: %s to any valid attribute", key));
        }
    }

    @JsonAnyGetter
    public Map<String, String> getTranslations() {
        Map<String, String> result = new HashMap<>();
        localizedNames.entrySet().forEach(e -> result.put("name#" + e.getKey(), e.getValue()));
        localizedDescriptions.entrySet().forEach(e -> result.put("description#" + e.getKey(), e.getValue()));
        return result;
    }


    @Override
    public String toString() {
        return "ScopeCreated{" +
                "scopeId='" + scopeId + '\'' +
                ", defaultName='" + defaultName + '\'' +
                ", defaultDescription='" + defaultDescription + '\'' +
                ", localizedNames=" + localizedNames +
                ", localizedDescriptions=" + localizedDescriptions +
                '}';
    }
}
