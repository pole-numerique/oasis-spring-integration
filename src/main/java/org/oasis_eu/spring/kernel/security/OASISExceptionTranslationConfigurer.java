package org.oasis_eu.spring.kernel.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.savedrequest.RequestCache;

/**
 * User: schambon
 * Date: 2/4/14
 */
public class OASISExceptionTranslationConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OASISExceptionTranslationConfigurer.class);

    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        RequestCache requestCache = http.getSharedObject(RequestCache.class);
        LOGGER.debug("Found request cache: " + requestCache);
        OASISExceptionTranslationFilter customFilter = new OASISExceptionTranslationFilter(authenticationEntryPoint, requestCache);

        http.addFilterAfter(customFilter, ExceptionTranslationFilter.class);
    }
}
