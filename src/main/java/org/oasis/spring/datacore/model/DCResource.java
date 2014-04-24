package org.oasis.spring.datacore.model;

import org.joda.time.DateTime;

import java.util.*;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 1/2/14
 */
public class DCResource {

    public static final int INITIAL_VERSION = -1;

    int version = INITIAL_VERSION;
    String baseUri;
    String type;
    String iri;

    DateTime created;
    DateTime lastModified;
    String createdBy;
    String lastModifiedBy;

    Map<String, Value> values = new HashMap<>();

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public String getType() {
        return type;
    }

    public void setType(String typeRef) {
        this.type = typeRef;
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(DateTime lastModified) {
        this.lastModified = lastModified;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Map<String, Value> getValues() {
        return values;
    }

    public void setUri(String uri) {
        setIri(uri.substring(uri.lastIndexOf('/') + 1));
        uri = uri.substring(0, uri.lastIndexOf('/'));
        setType(uri.substring(uri.lastIndexOf('/') + 1));
        uri = uri.substring(0, uri.lastIndexOf('/'));
        setBaseUri(uri);

    }

    public boolean isNew() {
        return version == -1;
    }

    public String getUri() {
        return baseUri + "/" + type + "/" + iri;
    }

    // Convenience methods follow

    public void set(String key, String value) {
        getValues().put(key, new StringValue(value));
    }

    public void set(String key, List<String> values) {

        getValues().put(key, new ArrayValue(values.stream().map(StringValue::new).collect(Collectors.toList())));


    }

    public String getAsString(String key) {
        Value val = getValues().get(key);
        if (val == null) return null;
        if (val.isArray()) {
            throw new IllegalArgumentException("Value for " + key + " is not a String but an Array");
        }
        return val.asString();
    }

    public List<String> getAsStringList(String key) {
        Value val = getValues().get(key);
        if (val.isArray()) {

            return val.asArray().stream().map(Value::asString).collect(Collectors.toList());

        } else {
            return Arrays.asList(val.asString());
        }

    }

    // poor design, but it's just for convenience anyway
    public abstract static class Value {
        public abstract boolean isArray();
        public abstract String asString();
        public abstract List<Value> asArray();
        public boolean isNull() {
            return false;
        }
        public boolean isString() {
            return !isArray();
        }
    }

    public static class StringValue extends Value {

        private String value;

        @Override
        public boolean isArray() {
            return false;
        }

        @Override
        public String asString() {
            return value;
        }

        @Override
        public List<Value> asArray() {
            throw new UnsupportedOperationException();
        }

        public void setValue(String value) {
            this.value = value;
        }
        public StringValue(String s) {
            value = s;
        }
        public StringValue() {

        }

        @Override
        public boolean isNull() {
            return value == null;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class ArrayValue extends Value {

        List<Value> values = new ArrayList<>();

        @Override
        public boolean isArray() {
            return true;
        }

        @Override
        public String asString() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Value> asArray() {
            return values;
        }

        public void setValues(List<Value> values) {
            this.values = values;
        }

        public ArrayValue(List<Value> values) {
            this.values = values;

        }
        public ArrayValue() {
            this.values = new ArrayList<>();
        }

        @Override
        public String toString() {
            return values.toString();
        }
    }
}
