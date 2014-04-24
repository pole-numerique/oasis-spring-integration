package org.oasis.spring.config;

import org.oasis.spring.kernel.security.OASISExceptionTranslationConfigurer;
import org.oasis.spring.kernel.security.OASISLogoutHandler;
import org.oasis.spring.kernel.security.OpenIdCAuthFilter;
import org.oasis.spring.kernel.security.OpenIdCAuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.client.RestTemplate;

/**
 * User: schambon
 * Date: 4/23/14
 */
@Configuration
@EnableWebSecurity
@Import(KernelConfiguration.class)
public abstract class OASISSecurityConfiguration extends WebSecurityConfigurerAdapter {

    // inject configuration
    @Value("${application.url}")
    String applicationUrl;
    @Value("${application.security.fetchUserInfo:false}")
    boolean fetchUserInfo;

    @Autowired
    @Qualifier("kernelRestTemplate")
    private RestTemplate kernelRestTemplate;

    @Bean
    public OpenIdCAuthProvider oasisAuthProvider() {
        OpenIdCAuthProvider provider = new OpenIdCAuthProvider();
        provider.setFetchUserInfo(fetchUserInfo);
        return provider;
    }

    @Bean
    @Qualifier("openIdConnectAuthenticationEntryPoint")
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new LoginUrlAuthenticationEntryPoint("/login");
    }

    @Bean
    public OpenIdCAuthFilter openIdCAuthFilter() throws Exception {
        OpenIdCAuthFilter filter = new OpenIdCAuthFilter();
        filter.setAuthenticationManager(authenticationManager());

        return filter;
    }

    @Bean
    protected OASISLogoutHandler logoutHandler() {
        OASISLogoutHandler handler = new OASISLogoutHandler();
        handler.setRestTemplate(kernelRestTemplate);
        handler.setAfterLogoutUrl(applicationUrl);
        return handler;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(oasisAuthProvider());
    }



    @Bean
    public OASISExceptionTranslationConfigurer oasisExceptionTranslationConfigurer() {
        return new OASISExceptionTranslationConfigurer();
    }


}
