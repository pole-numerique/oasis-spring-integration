package org.oasis_eu.spring.datacore.impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;
import org.oasis_eu.spring.datacore.model.DCResource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.Assert.*;

/**
 * User: schambon
 * Date: 1/2/14
 */
public class DCResourceTypeAdapterTest {

    @Test
    public void testDeserialization() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("dc_inner/resource.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

        DCResourceTypeAdapter adapter = new DCResourceTypeAdapter();
        JsonParser jp = new JsonParser();
        JsonElement element = jp.parse(reader);

        DCResource resource = adapter.deserialize(element, null, null);
        assertNotNull(resource);

        assertEquals("citizenkin.procedure.electoral_roll_registration", resource.getType());
        assertEquals("0000_1111_2222", resource.getIri());
        assertEquals("http://data-test.oasis_eu-eu.org/dc/type", resource.getBaseUri());
        assertEquals("Administrator", resource.getCreatedBy());
        assertEquals(ZonedDateTime.parse("2014-01-02T11:38:18.662+01:00").toInstant(), resource.getCreated());
        assertEquals("FRANCE", resource.getValues().get("pays_de_naissance").asString());
        assertEquals(0, resource.getVersion());
        assertTrue(resource.getValues().get("justificatifs_domicile").isArray());
        assertEquals("http://resources.citizenkin.org/dddd_eeee_ffff", resource.getValues().get("justificatifs_domicile").asArray().get(0).asString());
    }

    @Test
    public void testSerialization() throws Exception {
        DCResource resource = getDcResource();

        DCResourceTypeAdapter adapter = new DCResourceTypeAdapter();
        JsonElement elt = adapter.serialize(resource, null, null);

        assertNotNull(elt);
        assertTrue(elt.isJsonObject());
        JsonObject o = elt.getAsJsonObject();

        // there should not be a version
        assertNull(o.get("version"));
        assertEquals("http://data-test.oasis_eu-eu.org/dc/type/citizenkin.procedure.envelope/3333_4444_5555", o.get("@id").getAsString());
        assertEquals("electoral_roll_registration", o.get("definition_name").getAsString());
    }

    private DCResource getDcResource() {
        DCResource resource = new DCResource();
        resource.setType("citizenkin.procedure.envelope");
        resource.setIri("3333_4444_5555");
        resource.setBaseUri("http://data-test.oasis_eu-eu.org/dc/type");
        resource.setCreated(ZonedDateTime.now().withYear(2010).withMonth(01).withDayOfMonth(01).withHour(23).withMinute(30).toInstant());
        resource.getValues().put("state", new DCResource.StringValue("SENT"));
        resource.getValues().put("definition_name", new DCResource.StringValue("electoral_roll_registration"));
        resource.getValues().put("initiator", new DCResource.StringValue("tagada-tsouin-tsouin"));

        assertTrue(resource.isNew());
        return resource;
    }

    @Test
    public void testSerializationWithGson() {
        Gson gson = getGson();

        DCResource resource = getDcResource();

        String json = gson.toJson(resource);
        assertNotNull(json);
        assertTrue(json.contains("\"@id\":\"http://data-test.oasis_eu-eu.org/dc/type/citizenkin.procedure.envelope/3333_4444_5555"));
        assertTrue(json.contains("\"state\":\"SENT\""));
    }

    @Test
    public void testSerializationToWriter() {
        Gson gson = getGson();
        DCResource resource = getDcResource();

        StringWriter writer = new StringWriter();
        gson.toJson(resource, writer);

        String s = writer.toString();
        assertNotEquals("", s);
        assertTrue(s.contains("\"state\":\"SENT\""));
    }

    @Test
    public void testDeserializationWithGson() {
        Gson gson = getGson();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(getClass().getClassLoader().getResourceAsStream("dc_inner/resource.json"), StandardCharsets.UTF_8));
        DCResource resource = gson.fromJson(reader, DCResource.class);

        assertNotNull(resource);

        assertEquals("citizenkin.procedure.electoral_roll_registration", resource.getType());
        assertEquals("0000_1111_2222", resource.getIri());
        assertEquals("http://data-test.oasis_eu-eu.org/dc/type", resource.getBaseUri());
        assertEquals("Administrator", resource.getCreatedBy());
        assertEquals(ZonedDateTime.parse("2014-01-02T11:38:18.662+01:00").toInstant(), resource.getCreated());
        assertEquals("FRANCE", resource.getValues().get("pays_de_naissance").asString());
        assertEquals(0, resource.getVersion());
        assertTrue(resource.getValues().get("justificatifs_domicile").isArray());
        assertEquals("http://resources.citizenkin.org/dddd_eeee_ffff", resource.getValues().get("justificatifs_domicile").asArray().get(0).asString());

    }

    private Gson getGson() {
        return new GsonBuilder().registerTypeAdapter(DCResource.class, new DCResourceTypeAdapter()).create();
    }

    @Test
    public void testDeserializationOfMultipleResults() {
        Gson gson = getGson();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(getClass().getClassLoader().getResourceAsStream("dc_inner/multiple_resources.json"), StandardCharsets.UTF_8));

        List<DCResource> resources = gson.fromJson(reader, new TypeToken<List<DCResource>>(){}.getType());

        assertNotNull(resources);
        assertEquals(2, resources.size());
        assertEquals("33333_4444_5555", resources.get(0).getIri());
        assertEquals("6666_7777_8888", resources.get(1).getIri());
    }
}
