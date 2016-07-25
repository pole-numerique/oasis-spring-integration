package org.oasis_eu.spring.datacore.model;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 1/2/14
 */
public class DCResource {
    
    //////////////////////////////////////////////
    // TAKEN FROM DATACORE UriHelper
    // TODO LATER use it
    
    /** used to split id in order to encode its path elements if it's not disabled */
    public static final String URL_PATH_SEPARATOR = "/";
    public static final String URL_SAFE_CHARACTERS_BESIDES_ALPHANUMERIC = "\\$\\-_\\.\\+!\\*'\\(\\)";
    public static final String URL_SAFE_CHARACTERS_REGEX = "0-9a-zA-Z" + URL_SAFE_CHARACTERS_BESIDES_ALPHANUMERIC;
    public static final String URL_ALSO_SAFE_PATH_SEGMENT_OR_SLASH_CHARACTERS = ":@~&,;=/";
    public static final String URL_SAFE_PATH_SEGMENT_OR_SLASH_CHARACTERS_REGEX = URL_SAFE_CHARACTERS_REGEX
            + URL_ALSO_SAFE_PATH_SEGMENT_OR_SLASH_CHARACTERS;
    public static final String URL_SAFE_PATH_SEGMENT_OR_SLASH_CHARACTERS_BESIDES_ALPHANUMERIC
            = URL_SAFE_CHARACTERS_BESIDES_ALPHANUMERIC + URL_ALSO_SAFE_PATH_SEGMENT_OR_SLASH_CHARACTERS;
    /** IRI rule, other characters must be encoded. NB. : are required for ex. prefixed field names */
    public static final String NOT_URL_SAFE_PATH_SEGMENT_OR_SLASH_CHARACTERS_REGEX = "[^"
            + URL_SAFE_PATH_SEGMENT_OR_SLASH_CHARACTERS_REGEX + "]";
    /** model name & type best practice rule, other characters are forbidden */
    public static final String NOT_URL_ALWAYS_SAFE_OR_COLON_CHARACTERS_REGEX =
          "[^0-9a-zA-Z\\$\\-_\\.\\(\\)\\:]"; // not reserved +!*, not '

    /** to detect whether relative (rather than absolute) uri
     groups are delimited by () see http://stackoverflow.com/questions/6865377/java-regex-capture-group
     URI scheme : see http://stackoverflow.com/questions/3641722/valid-characters-for-uri-schemes */
    private static final Pattern anyBaseUrlPattern = Pattern.compile("^([a-zA-Z][a-zA-Z0-9\\.\\-\\+]*)://[^/]+"); // TODO or "^http[s]?://data\\.ozwillo\\.com/" ?
    private static final Pattern multiSlashPattern = Pattern.compile("/+");
    private static final Pattern frontSlashesPattern = Pattern.compile("^/*");
    private static final Pattern notUrlAlwaysSafeCharactersPattern = Pattern.compile(NOT_URL_ALWAYS_SAFE_OR_COLON_CHARACTERS_REGEX);
    private static final Pattern notIriSafeCharactersPattern = Pattern.compile(NOT_URL_SAFE_PATH_SEGMENT_OR_SLASH_CHARACTERS_REGEX);
    
    

    public static final int INITIAL_VERSION = -1;

    int version = INITIAL_VERSION;
    /** (encoded) */
    String baseUri;
    /** (decoded) */
    String type;
    /** (encoded) */
    String iri;

    /** cache (encoded) */
    String uri;
    /** cache */
    String encodedType;

    Instant created;
    Instant lastModified;
    String createdBy;
    String lastModifiedBy;

    Map<String, Value> values = new HashMap<>();
    
    public static String encodeUriPathSegment(String uriPathSegment) {
        StringBuilder sb = new StringBuilder();
        try {
            for (char c : uriPathSegment.toCharArray()) {
                if ( (c >= 48 && c <= 57) // number
                        || (c >= 65 && c <= 90) // upper case
                        || (c >= 97 && c <= 122) // lower case
                        || URL_SAFE_PATH_SEGMENT_OR_SLASH_CHARACTERS_BESIDES_ALPHANUMERIC.indexOf(c) != -1) { // among safe chars
                    sb.append(c);
                } else {
                    sb.append(URLEncoder.encode(new String(Character.toChars(c)) , "UTF-8"));
                }
            }
        } catch (UnsupportedEncodingException e) {
            // should never happens for UTF-8
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    private static char toHex(int c) {
       return (char) (c < 10 ? '0' + c : 'A' + c - 10);
    }

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
        if (this.encodedType == null) {
            this.encodedType = encodeUriPathSegment(this.type);
        }
        return this.encodedType;
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
    /**
     * Can also be used to parse an uri by creating a dummy Resource and setting its uri
     * @param uri
     */
    public void setUri(String uri) {
        int modelTypeIndex = uri.indexOf(dcTypeMidfix) + dcTypeMidfix.length();
        int idSlashIndex = uri.indexOf('/', modelTypeIndex);
        String encodedType = uri.substring(modelTypeIndex, idSlashIndex);
        try {
            setType(URLDecoder.decode(encodedType, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // should never happens for UTF-8
            throw new RuntimeException(e);
        }
        
        setIri(uri.substring(idSlashIndex+1)); // (encoded)
        setBaseUri(uri.substring(0, modelTypeIndex-1)); // (encoded)
    }

    /**
     * 
     * @return (encoded)
     */
    public String getUri() {
        if (this.uri == null) {
            this.uri = baseUri + '/' + getEncodedType() + '/' + getIri();
        }
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
        if (val == null)
            return Collections.emptyList();
        else if (val.isArray()) {
            return val.asArray().stream().map(Value::asString).collect(Collectors.toList());
        } else {
            return Collections.singletonList(val.asString());
        }
    }

    public Map<String, String> getAsStringMap(String key) {
        Value val = getValues().get(key);
        if (val.isMap()) {
            Map<String, String> map = new HashMap<>();
            val.asMap().forEach((key1, value) -> map.put(key1, value.asString()));
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

    @Override
    public String toString() {
        return "DCResource{" +
                   "version=" + version +
                   ", baseUri='" + baseUri + '\'' +
                   ", type='" + type + '\'' +
                   ", iri='" + iri + '\'' +
                   ", uri='" + uri + '\'' +
                   ", encodedType='" + encodedType + '\'' +
                   ", created=" + created +
                   ", lastModified=" + lastModified +
                   ", createdBy='" + createdBy + '\'' +
                   ", lastModifiedBy='" + lastModifiedBy + '\'' +
                   ", values=" + values +
                   '}';
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
