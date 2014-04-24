package org.oasis.spring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.oasis.spring.datacore.impl.DCResourceTypeAdapter;
import org.oasis.spring.datacore.impl.DCRightsTypeAdapter;
import org.oasis.spring.datacore.impl.DatacoreSecurityInterceptor;
import org.oasis.spring.datacore.impl.GsonMessageConverter;
import org.oasis.spring.datacore.model.DCResource;
import org.oasis.spring.datacore.model.DCRights;
import org.oasis.spring.kernel.OpenIdCConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@ComponentScan(basePackages = {"org.oasis.spring.kernel", "org.oasis.spring.datacore"})
public class KernelConfiguration {

    // inject configuration
    @Value("${kernel.auth.issuer}")
    String authIssuer;
    @Value("${kernel.auth.auth_endpoint}")
    String authAuthEndpoint;
    @Value("${kernel.auth.token_endpoint}")
    String authTokenEndpoint;
    @Value("${kernel.auth.keys_endpoint}")
    String authKeysEndpoint;
    @Value("${kernel.auth.revoke_endpoint}")
    String authRevocationEndpoint;
    @Value("${kernel.auth.userinfo_endpoint}")
    String authUserInfoEndpoint;
    @Value("${kernel.auth.callback_uri}")
    String callbackUri;
    @Value("${kernel.application_id}")
    String applicationId;
    @Value("${kernel.client_id}")
    String clientId;
    @Value("${kernel.client_secret}")
    String clientSecret;
    @Value("${kernel.scopes_to_require}")
    String scopesToRequire;

    @Bean
    public OpenIdCConfiguration openIdCConfiguration() {
        OpenIdCConfiguration configuration = new OpenIdCConfiguration();
        configuration.setIssuer(authIssuer);
        configuration.setAuthEndpoint(authAuthEndpoint);
        configuration.setTokenEndpoint(authTokenEndpoint);
        configuration.setKeysEndpoint(authKeysEndpoint);
        configuration.setRevocationEndpoint(authRevocationEndpoint);
        configuration.setUserInfoEndpoint(authUserInfoEndpoint);
        configuration.setCallbackUri(callbackUri);
        configuration.setApplicationId(applicationId);
        configuration.setClientId(clientId);
        configuration.setClientSecret(clientSecret);
        configuration.setScopesToRequire(scopesToRequire);
        return configuration;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JSR310Module());
        return objectMapper;
    }

    @Bean
    @Qualifier("kernelRestTemplate")
    public RestTemplate kernelRestTemplate() {
        RestTemplate template = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();

        messageConverters.add(new FormHttpMessageConverter());
        messageConverters.add(new StringHttpMessageConverter());

        MappingJackson2HttpMessageConverter jacksonMessageConverter = new MappingJackson2HttpMessageConverter();
        jacksonMessageConverter.setObjectMapper(objectMapper());
        messageConverters.add(jacksonMessageConverter);

        template.setMessageConverters(messageConverters);

        return template;
    }


    /* data core client */
    @Bean
    @Qualifier("dataCore")
    public RestTemplate dataCoreRestTemplate(Gson dataCoreGson) {
        RestTemplate template = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(new GsonMessageConverter(dataCoreGson));
        template.setMessageConverters(messageConverters);

        template.setInterceptors(Arrays.asList(datacoreSecurityInterceptor()));

        return template;
    }

    @Bean
    public Gson dataCoreGson() {
        return new GsonBuilder().registerTypeAdapter(DCResource.class, new DCResourceTypeAdapter())
                .registerTypeAdapter(DCRights.class, new DCRightsTypeAdapter())
//                .registerTypeAdapter(AgentInfo.class, new AgentInfoTypeAdapter())
//                .registerTypeAdapter(AgentInfoAddress.class, new AgentInfoAddressTypeAdapter())
//                .registerTypeAdapter(AgentListWrapper.class, new AgentListWrapperTypeAdapter())
//                .registerTypeAdapter(Group.class, new GroupTypeAdapter())
//                .registerTypeAdapter(Event.class, new EventTypeAdapter())
//                .registerTypeAdapter(ProcedurePublishedEventData.class, new ProcedurePublishedEventDataTypeAdapter())
//                .registerTypeAdapter(Subscription.class, new SubscriptionTypeAdapter())
                .create();
    }



    @Bean
    public ClientHttpRequestInterceptor datacoreSecurityInterceptor() {
        return new DatacoreSecurityInterceptor();
    }

}
