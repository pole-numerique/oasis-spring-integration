package org.oasis_eu.spring.datacore.model;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 1/2/14
 */
public class DCResource {

    public static final int INITIAL_VERSION = -1;

    int version = INITIAL_VERSION;
    /** (encoded) */
    String uri;
    /** (encoded) */
    String baseUri;
    String encodedType;
    /** (decoded) */
    String type;
    /** (encoded) */
    String iri;

    Instant created;
    Instant lastModified;
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

    public String getEncodedType() {
        return encodedType;
    }

    public void setEncodedType(String encodedTypeRef) {
        this.encodedType = encodedTypeRef;
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

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(Instant lastModified) {
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

    public static String dcTypeMidfix = "/dc/type/";
    public void setUri(String uri) {
        this.uri = uri;
        
        int modelTypeIndex = uri.indexOf(dcTypeMidfix) + dcTypeMidfix.length();
        int idSlashIndex = uri.indexOf('/', modelTypeIndex);
        this.encodedType = uri.substring(modelTypeIndex, idSlashIndex);
        try {
            setType(URLDecoder.decode(this.encodedType, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // should never happens for UTF-8
        }
        
        setIri(uri.substring(idSlashIndex+1)); // (encoded)
        setBaseUri(uri.substring(0, modelTypeIndex-1)); // (encoded)
    }

    public String getUri() {
        return uri;
    }

    public boolean isNew() {
        return version == -1;
    }

    // Convenience methods follow

    public void set(String key, String value) {
        getValues().put(key, new StringValue(value));
    }

    public void setMappedList(String key, List<Map<String,Value>> values) {
        getValues().put(key, new ArrayValue(values.stream().map(MapValue::new).collect(Collectors.toList())));
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

    public Map<String, String> getAsStringMap(String key) {
        Value val = getValues().get(key);
        if (val.isMap()) {
            Map<String, String> map = new HashMap<String,String>();
            val.asMap().entrySet().stream().map(entry -> map.put(entry.getKey(), entry.getValue().asString()));
            return map;

        } else {
            throw new UnsupportedOperationException();
        }

    }

    public Object get(String key) {
        return toObject(getValues().get(key));
    }
    
    private Object toObject(Value val) {
        if (val == null) return val;
        if (val.isMap()) {
            return val.asMap().entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> toObject(entry.getValue())));
        } else if (val.isArray()) {
            return val.asArray().stream().map(this::toObject).collect(Collectors.toList());
        } else {
            return val.asString();
        }
    }

    // poor design, but it's just for convenience anyway
    public abstract static class Value {
        public abstract boolean isArray();
        public abstract boolean isMap();
        public abstract String asString();
        public abstract List<Value> asArray();
        public abstract Map<String,Value> asMap();
        public boolean isNull() {
            return false;
        }
        public boolean isString() {
            return !isArray() && !isMap();
        }
    }

    public static class StringValue extends Value {

        private String value;

        public StringValue(String s) {
            value = s;
        }
        public StringValue() {}

        @Override
        public boolean isArray() {
            return false;
        }

        @Override
        public boolean isMap() {
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

        @Override
        public Map<String,Value> asMap() {
            throw new UnsupportedOperationException();
        }

        public void setValue(String value) {
            this.value = value;
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
        public boolean isMap() {
            return false;
        }

        @Override
        public String asString() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Value> asArray() {
            return values;
        }

        @Override
        public Map<String,Value> asMap() {
            throw new UnsupportedOperationException();
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

    public static class MapValue extends Value {

        Map<String,Value> values = new HashMap<>();

        @Override
        public boolean isArray() {
            return false;
        }

        @Override
        public boolean isMap() {
            return true;
        }

        @Override
        public String asString() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Value> asArray() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String,Value> asMap() {
            return values;
        }

        public void setValues(Map<String,Value> values) {
            this.values = values;
        }

        public MapValue(Map<String,Value> values) {
            this.values = values;

        }
        public MapValue() {
            this.values = new HashMap<>();
        }

        @Override
        public String toString() {
            return values.toString();
        }
    }
    
}
