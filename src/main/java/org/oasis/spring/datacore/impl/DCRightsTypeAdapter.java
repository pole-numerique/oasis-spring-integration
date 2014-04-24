package org.oasis.spring.datacore.impl;

import com.google.gson.*;
import org.oasis.spring.datacore.model.DCRights;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
            List<String> owners = new ArrayList<>(ownersArray.size());
            for (int i = 0; i < ownersArray.size(); i++) {
                owners.add(ownersArray.get(i).getAsString());
            }
            rights.setOwners(owners);
        }
        
        if (object.has("writers")) {
            JsonArray writersArray = object.getAsJsonArray("writers");
            List<String> writers = new ArrayList<>(writersArray.size());
            for (int i = 0; i < writersArray.size(); i++) {
                writers.add(writersArray.get(i).getAsString());
            }
            rights.setWriters(writers);
        }
        
        if (object.has("readers")) {
            JsonArray readersArray = object.getAsJsonArray("readers");
            List<String> readers = new ArrayList<>(readersArray.size());
            for (int i = 0; i < readersArray.size(); i++) {
                readers.add(readersArray.get(i).getAsString());
            }
            rights.setReaders(readers);

        }
        
        return rights;
    }

}
