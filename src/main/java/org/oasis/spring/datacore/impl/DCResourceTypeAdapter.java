package org.oasis.spring.datacore.impl;

import com.google.gson.*;
import org.oasis.spring.datacore.model.DCResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * User: schambon
 * Date: 1/2/14
 */
public class DCResourceTypeAdapter implements JsonSerializer<DCResource>, JsonDeserializer<DCResource> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DCResourceTypeAdapter.class);

    private static final Map<String, PropertyHelper> BUILTIN_PROPERTIES = new HashMap<>();
    static {
        BUILTIN_PROPERTIES.put("uri", new StringHelper());
        BUILTIN_PROPERTIES.put("version", new VersionHelper());
        BUILTIN_PROPERTIES.put("created", new DateHelper());
        BUILTIN_PROPERTIES.put("lastModified", new DateHelper());
        BUILTIN_PROPERTIES.put("createdBy", new StringHelper());
        BUILTIN_PROPERTIES.put("lastModifiedBy", new StringHelper());
    }

    @Override
    public JsonElement serialize(DCResource resource, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject o = new JsonObject();

        o.add("uri", new JsonPrimitive(resource.getUri()));
        if (! resource.isNew()) {
            o.add("version", new JsonPrimitive(resource.getVersion()));
        }

        resource.getValues().forEach((k, v) -> {
            if (! v.isNull()) {
                o.add(k, toJson(v));
            }
        });

        return o;
    }

    private JsonElement toJson(DCResource.Value value) {
        if (value.isArray()) {
            JsonArray array = new JsonArray();
            value.asArray().forEach(val -> array.add(toJson(val)));
            return array;
        } else return new JsonPrimitive(value.asString());
    }

    @Override
    public DCResource deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        DCResource resource = new DCResource();

        JsonObject object = jsonElement.getAsJsonObject();
        for (Map.Entry<String, JsonElement> child : object.entrySet()) {
            if (BUILTIN_PROPERTIES.containsKey(child.getKey())) {
                BUILTIN_PROPERTIES.get(child.getKey()).set(child.getValue(), resource, child.getKey());
            } else {
                setDataField(resource, child.getKey(), child.getValue());
            }

        }

        return resource;
    }

    private void setDataField(DCResource resource, String name, JsonElement value) {
        if (value.isJsonArray()) {
            resource.getValues().put(name, getValue(value));
        } else {
            resource.getValues().put(name, new DCResource.StringValue(value.getAsString()));
        }
    }

    private DCResource.Value getValue(JsonElement value) {
        if (value.isJsonArray()) {
            DCResource.ArrayValue v = new DCResource.ArrayValue();

            for (JsonElement element : value.getAsJsonArray()) {
                v.asArray().add(getValue(element));
            }
            return v;
        } else {
            return new DCResource.StringValue(value.getAsString());
        }
    }

    private static abstract class PropertyHelper<T> {
        abstract T read(JsonElement input);

        abstract Class<T> getArgClass();

        void set(JsonElement input, DCResource target, String name) {
            try {
                Method method = DCResource.class.getMethod("set" + StringUtils.capitalize(name), getArgClass());
                method.invoke(target, read(input));
            } catch (Exception nosuch) {
                LOGGER.error("Cannot set builtin property", nosuch);
            }
        }
    }

    private static class DateHelper extends PropertyHelper<Instant> {
        @Override
        public Instant read(JsonElement input) {


            return ZonedDateTime.parse(input.getAsString()).toInstant();
        }

        @Override
        Class getArgClass() {
            return Instant.class;
        }
    }

    private static class StringHelper extends PropertyHelper<String> {
        @Override
        public String read(JsonElement input) {
            return input.getAsString();
        }

        @Override
        Class getArgClass() {
            return String.class;
        }
    }

    private static class VersionHelper extends PropertyHelper<Integer> {
        @Override
        public Integer read(JsonElement input) {
            return input.getAsInt();
        }

        @Override
        Class getArgClass() {
            return int.class;
        }
    }



}
