package org.oasis_eu.spring.config;

import org.oasis_eu.spring.kernel.security.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * User: schambon
 * Date: 4/23/14
 */
@Configuration
@EnableWebSecurity
@Import(KernelConfiguration.class)
public abstract class OasisSecurityConfiguration extends WebSecurityConfigurerAdapter {

    // inject configuration
    @Value("${application.security.fetchUserInfo:false}")
    boolean fetchUserInfo;

    @Bean
    public OpenIdCAuthProvider oasisAuthProvider() {
        OpenIdCAuthProvider provider = new OpenIdCAuthProvider();
        provider.setFetchUserInfo(fetchUserInfo);
        return provider;
    }

    @Bean
    @Qualifier("openIdConnectAuthenticationEntryPoint")
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new OasisLoginUrlAuthenticationEntryPoint("/login");
    }

    @Bean
    public OasisAuthenticationFilter oasisAuthenticationFilter() throws Exception {
        OasisAuthenticationFilter oasisAuthenticationFilter = new OasisAuthenticationFilter();
        oasisAuthenticationFilter.setAuthenticationManager(authenticationManager());
        return oasisAuthenticationFilter;
    }

    @Bean
    protected OasisLogoutHandler logoutHandler() {
        return new OasisLogoutHandler();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(oasisAuthProvider());
    }

    @Bean
    public OasisExceptionTranslationFilter oasisExceptionTranslationFilter(AuthenticationEntryPoint authenticationEntryPoint) {
        return new OasisExceptionTranslationFilter(authenticationEntryPoint);
    }

}
