package org.oasis_eu.spring.kernel.model;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.none;
import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;
import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.client;

public class AuthenticationBuilderTest {

    @Test
    public void testPublic() {
        Authentication p = none();
        assertFalse(p.hasAuthenticationHeader());
    }

    @Test
    public void testUser() {
        Authentication u = user("accesstoken");
        assertTrue(u.hasAuthenticationHeader());
        assertEquals("Bearer accesstoken", u.getAuthenticationHeader());
    }

    @Test
    public void testClient() {
        Authentication client = client("clientid", "clientsecret");
        assertTrue(client.hasAuthenticationHeader());
        assertEquals("Basic Y2xpZW50aWQ6Y2xpZW50c2VjcmV0", client.getAuthenticationHeader());
    }



}