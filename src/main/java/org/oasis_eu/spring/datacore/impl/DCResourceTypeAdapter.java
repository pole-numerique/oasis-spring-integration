package org.oasis_eu.spring.datacore.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.oasis_eu.spring.datacore.model.DCResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * User: schambon
 * Date: 1/2/14
 */
public class DCResourceTypeAdapter implements JsonSerializer<DCResource>, JsonDeserializer<DCResource> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DCResourceTypeAdapter.class);

    private static final Map<String, PropertyHelper> BUILTIN_PROPERTIES = new HashMap<>();
    static {
        BUILTIN_PROPERTIES.put("@id", new StringHelper("uri"));
        BUILTIN_PROPERTIES.put("o:version", new VersionHelper("version"));
        BUILTIN_PROPERTIES.put("dc:created", new DateHelper("created"));
        BUILTIN_PROPERTIES.put("dc:modified", new DateHelper("lastModified"));
        BUILTIN_PROPERTIES.put("dc:creator", new StringHelper("createdBy"));
        BUILTIN_PROPERTIES.put("dc:contributor", new StringHelper("lastModifiedBy"));
    }

    @Override
    public JsonElement serialize(DCResource resource, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject o = new JsonObject();

        o.add("@id", new JsonPrimitive(resource.getUri()));
        if (! resource.isNew()) {
            o.add("o:version", new JsonPrimitive(resource.getVersion()));
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
        } else if (value.isMap()) {
            JsonObject object = new JsonObject();
            value.asMap().forEach((key,val) -> object.add(key, toJson(val)));
            return object;
        } else return new JsonPrimitive(value.asString());
    }

    @Override
    public DCResource deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        DCResource resource = new DCResource();

        JsonObject object = jsonElement.getAsJsonObject();
        for (Map.Entry<String, JsonElement> child : object.entrySet()) {
            if (BUILTIN_PROPERTIES.containsKey(child.getKey())) {
                BUILTIN_PROPERTIES.get(child.getKey()).set(child.getValue(), resource);
            } else {
                setDataField(resource, child.getKey(), child.getValue());
            }

        }

        return resource;
    }

    private void setDataField(DCResource resource, String name, JsonElement value) {
        if (value.isJsonArray() || value.isJsonObject()) {
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
        } else if (value.isJsonObject()) {
            DCResource.MapValue v = new DCResource.MapValue();
            for (Map.Entry<String,JsonElement> entry : value.getAsJsonObject().entrySet()) { v.asMap().put(entry.getKey(), getValue(entry.getValue())); }
            //value.getAsJsonObject().entrySet().stream().map(entry -> v.asMap().put(entry.getKey(), getValue(entry.getValue())));
            return v;
        } else {
            return new DCResource.StringValue(value.getAsString());
        }
    }

    private static abstract class PropertyHelper<T> {

        protected PropertyHelper(String propertyName) {
            this.propertyName = propertyName;
        }

        public String propertyName;

        public String getPropertyName() {
            return propertyName;
        }

        abstract T read(JsonElement input);

        abstract Class<T> getArgClass();

        void set(JsonElement input, DCResource target) {
            try {
                Method method = DCResource.class.getMethod("set" + StringUtils.capitalize(propertyName), getArgClass());
                method.invoke(target, read(input));
            } catch (Exception nosuch) {
                LOGGER.error("Cannot set builtin property", nosuch);
            }
        }
    }

    private static class DateHelper extends PropertyHelper<Instant> {
        public DateHelper(String propertyName) {
            super(propertyName);
        }

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
        public StringHelper(String propertyName) {
            super(propertyName);
        }

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
        public VersionHelper(String propertyName) {
            super(propertyName);
        }

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
