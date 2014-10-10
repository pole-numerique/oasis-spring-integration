package org.oasis_eu.spring.kernel.security;

import org.junit.Test;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class OasisAuthenticationFilterTest {

    @Test
    public void testSkipAuthentication() throws Exception {

        HttpServletRequest req = mock(HttpServletRequest.class);
        String url = "/whatever/is/my?great=answer&final=one";
        stub(req.getServletPath()).toReturn(url);

        OpenIdCConfiguration conf = mock(OpenIdCConfiguration.class);
        when(conf.skipAuthenticationForUrl(url)).thenReturn(true);

        FilterChain chain = mock(FilterChain.class);

        OasisAuthenticationFilter filter = new OasisAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "configuration", conf);

        filter.doFilter(req, null, chain);

        verify(chain).doFilter(req, null);
        verify(conf).skipAuthenticationForUrl(url);
    }

    @Test
    public void testDoNotSkip() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        String url = "/whatever/is/my?great=answer&final=one";
        stub(req.getServletPath()).toReturn(url);

        OpenIdCConfiguration conf = mock(OpenIdCConfiguration.class);

        FilterChain chain = mock(FilterChain.class);

        OasisAuthenticationFilter filter = new OasisAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "configuration", conf);

        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(ctx);

        RequestCache cache = mock(RequestCache.class);
        OpenIdCService service = mock(OpenIdCService.class);

        HttpSession session = mock(HttpSession.class);
        when(req.getSession()).thenReturn(session);
        when(session.isNew()).thenReturn(true);

        ReflectionTestUtils.setField(filter, "requestCache", cache);
        ReflectionTestUtils.setField(filter, "openIdCService", service);

        filter.doFilter(req, null, chain);

        verify(conf).skipAuthenticationForUrl(url);
        verify(cache).saveRequest(req, null);
        verify(service).redirectToAuth(req, null, StateType.SIMPLE_CHECK);

    }
}