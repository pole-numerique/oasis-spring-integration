package org.oasis.spring.datacore.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import org.oasis.spring.datacore.model.DCResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * User: schambon
 * Date: 1/3/14
 */
public class GsonMessageConverterTest {

    Gson gson = new GsonBuilder().registerTypeAdapter(DCResource.class, new DCResourceTypeAdapter()).create();
    @Test
    public void testRead() throws IOException {
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getClassLoader().getResourceAsStream("dc_inner/resource.json"));

        GsonMessageConverter underTest = new GsonMessageConverter(gson);

        DCResource resource = (DCResource) underTest.read(DCResource.class, null, message);
        assertEquals("citizenkin.procedure.electoral_roll_registration", resource.getType());
        assertEquals("0000_1111_2222", resource.getIri());
        assertEquals("http://data-test.oasis-eu.org/dc/type", resource.getBaseUri());
        assertEquals("Administrator", resource.getCreatedBy());
        assertEquals(ZonedDateTime.parse("2014-01-02T11:38:18.662+01:00").toInstant(), resource.getCreated());
        assertEquals("FRANCE", resource.getValues().get("pays_de_naissance").asString());
        assertEquals(0, resource.getVersion());
        assertTrue(resource.getValues().get("justificatifs_domicile").isArray());
        assertEquals("http://resources.citizenkin.org/dddd_eeee_ffff", resource.getValues().get("justificatifs_domicile").asArray().get(0).asString());

    }


    @Test
    public void testWrite() throws IOException {
        HttpOutputMessage message = mock(HttpOutputMessage.class);
        HttpHeaders headers = mock(HttpHeaders.class);


        when(message.getHeaders()).thenReturn(headers);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(4096);
        when(message.getBody()).thenReturn(byteArrayOutputStream);

        GsonMessageConverter underTest = new GsonMessageConverter(gson);

        DCResource resource = new DCResource();
        resource.setType("citizenkin.procedure.envelope");
        resource.setIri("3333_4444_5555");
        resource.setBaseUri("http://data-test.oasis-eu.org/dc/type");
        resource.setCreated(ZonedDateTime.now().withYear(2010).withMonth(01).withDayOfMonth(01).withHour(23).withMinute(30).toInstant());
        resource.getValues().put("state", new DCResource.StringValue("SENT"));
        resource.getValues().put("definition_name", new DCResource.StringValue("electoral_roll_registration"));
        resource.getValues().put("initiator", new DCResource.StringValue("tagada-tsouin-tsouin"));


        underTest.write(resource, MediaType.APPLICATION_JSON, message);

        byte[] bytes = byteArrayOutputStream.toByteArray();
        assertNotEquals(0, bytes.length);

        String s = new String(bytes);
        assertTrue(s.contains("\"state\":\"SENT\""));

        verify(message, atLeastOnce()).getHeaders();
        verify(headers, atLeastOnce()).setContentType(MediaType.APPLICATION_JSON);
        verify(message, atLeastOnce()).getBody();

    }
}
