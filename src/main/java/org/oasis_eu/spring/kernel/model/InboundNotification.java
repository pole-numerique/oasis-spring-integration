package org.oasis_eu.spring.kernel.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class InboundNotification {
	private static final Logger logger = LoggerFactory.getLogger(InboundNotification.class);

	@JsonProperty("id")
	private String id;

	@JsonProperty("user_id")
	private String userId;

	@JsonProperty("instance_id")
	private String instanceId;

	@JsonProperty("message")
	private String message;
	private Map<String, String> localizedmessages = new HashMap<>();

	@JsonProperty("service_id")
	private String serviceId;

	@JsonProperty("action_uri")
	private String actionUri;

	@JsonProperty("action_label")
	private String actionLabel;

	@JsonProperty("status")
	private NotificationStatus status;

	@JsonProperty("time")
	private Instant time;

	@JsonAnyGetter
	public Map<String, String> anyGetter() {
		Map<String, String> result = new HashMap<>();

		localizedmessages.entrySet().forEach(e -> result.put("message#" + e.getKey(), e.getValue()));

		return result;
	}

	@JsonAnySetter
	public void anySetter(String key, String value) {
		if (key.startsWith("message#")) {
			localizedmessages.put(key.substring("message#".length()), value);
		} else {
			logger.debug("Discarding unknown property {}", key);
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage(Locale locale) {
		if (localizedmessages.containsKey(locale.toLanguageTag())) {
			return localizedmessages.get(locale.toLanguageTag());
		} else {
			return message;
		}
	}

	@JsonIgnore
	public void setLocalizedMessage(Map<String, String> localizedmessages) {
		this.localizedmessages = localizedmessages;
	}

	public String getActionUri() {
		return actionUri;
	}

	public void setActionUri(String actionUri) {
		this.actionUri = actionUri;
	}

	public String getActionLabel() {
		return actionLabel;
	}

	public void setActionLabel(String actionLabel) {
		this.actionLabel = actionLabel;
	}

	public NotificationStatus getStatus() {
		return status;
	}

	public void setStatus(NotificationStatus status) {
		this.status = status;
	}

	public Instant getTime() {
		return time;
	}

	public void setTime(Instant time) {
		this.time = time;
	}
}
