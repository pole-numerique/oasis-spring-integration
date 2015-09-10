package org.oasis_eu.spring.datacore.impl;

import java.lang.reflect.Type;
import java.util.LinkedHashSet;

import org.oasis_eu.spring.datacore.model.DCRights;

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
 * User: flombard
 * Date: 24/2/14
 */
public class DCRightsTypeAdapter implements JsonSerializer<DCRights>, JsonDeserializer<DCRights> {

//    private static final Logger LOGGER = LoggerFactory.getLogger(DCRightsTypeAdapter.class);

    @Override
    public JsonElement serialize(DCRights rights, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject o = new JsonObject();
        
        if (rights.getOwners() != null) {
            JsonArray ownersArray = new JsonArray();
            for (String owner : rights.getOwners()) {
                ownersArray.add(new JsonPrimitive(owner));
            }
            o.add("owners", ownersArray);
        }
        
        if (rights.getReaders() != null) {
            JsonArray readersArray = new JsonArray();
            for (String reader : rights.getReaders()) {
                readersArray.add(new JsonPrimitive(reader));
            }
            o.add("readers", readersArray);
        }
        
        if (rights.getWriters() != null) {
            JsonArray writersArray = new JsonArray();
            for (String writer : rights.getWriters()) {
                writersArray.add(new JsonPrimitive(writer));
            }
            o.add("writers", writersArray);
        }
        
        return o;
    }

    @Override
    public DCRights deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        DCRights rights = new DCRights();
        
        JsonObject object = jsonElement.getAsJsonObject();
        
        if (object.has("owners")) {
            JsonArray ownersArray = object.getAsJsonArray("owners");
            LinkedHashSet<String> owners = new LinkedHashSet<>(ownersArray.size());
            for (int i = 0; i < ownersArray.size(); i++) {
                owners.add(ownersArray.get(i).getAsString());
            }
            rights.setOwners(owners);
        }
        
        if (object.has("writers")) {
            JsonArray writersArray = object.getAsJsonArray("writers");
            LinkedHashSet<String> writers = new LinkedHashSet<>(writersArray.size());
            for (int i = 0; i < writersArray.size(); i++) {
                writers.add(writersArray.get(i).getAsString());
            }
            rights.setWriters(writers);
        }
        
        if (object.has("readers")) {
            JsonArray readersArray = object.getAsJsonArray("readers");
            LinkedHashSet<String> readers = new LinkedHashSet<>(readersArray.size());
            for (int i = 0; i < readersArray.size(); i++) {
                readers.add(readersArray.get(i).getAsString());
            }
            rights.setReaders(readers);

        }
        
        return rights;
    }

}
