package org.oasis_eu.spring.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.oasis_eu.spring.datacore.impl.DCResourceTypeAdapter;
import org.oasis_eu.spring.datacore.impl.DCRightsTypeAdapter;
import org.oasis_eu.spring.datacore.impl.DatacoreSecurityInterceptor;
import org.oasis_eu.spring.datacore.model.DCResource;
import org.oasis_eu.spring.datacore.model.DCRights;
import org.oasis_eu.spring.kernel.rest.KernelResponseErrorHandler;
import org.oasis_eu.spring.kernel.security.OpenIdCConfiguration;
import org.oasis_eu.spring.kernel.security.StaticOpenIdCConfiguration;
import org.oasis_eu.spring.util.KernelLoggingInterceptor;
import org.oasis_eu.spring.util.NullConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@ComponentScan(basePackages = {"org.oasis_eu.spring.kernel", "org.oasis_eu.spring.datacore"})
public class KernelConfiguration {

    @Value("${rest.revertToApacheHttpComponentsClient:false}") private boolean revertToApacheHttpComponentsClient = false;

    @Bean
    public OpenIdCConfiguration openIdCConfiguration() {
        OpenIdCConfiguration configuration = new StaticOpenIdCConfiguration();
        return configuration;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new JodaModule());

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
    
    private ClientHttpRequestFactory newRequestFactory() {
        if (revertToApacheHttpComponentsClient) {
            // revert to using Apache HTTPComponents Client :
            // old default, would have required specific conf for enabling SNI (which should be possible since 4.3.2)
            return new HttpComponentsClientHttpRequestFactory();
        } else {
            // use mere HttpURLConnection, better : https://jira.spring.io/browse/ANDROID-54 http://stackoverflow.com/questions/25698072/simpleclienthttprequestfactory-vs-httpcomponentsclienthttprequestfactory-for-htt
            return new SimpleClientHttpRequestFactory();
        }
    }

    @Bean
    @Qualifier("kernelRestTemplate")
    public RestTemplate kernelRestTemplate() {
        RestTemplate template = new RestTemplate(newRequestFactory());

        template.setInterceptors(Arrays.asList(new KernelLoggingInterceptor()));

        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();

        messageConverters.add(new FormHttpMessageConverter());
        messageConverters.add(new StringHttpMessageConverter());

        MappingJackson2HttpMessageConverter jacksonMessageConverter = new MappingJackson2HttpMessageConverter();
        jacksonMessageConverter.setObjectMapper(objectMapper());
        messageConverters.add(jacksonMessageConverter);

        messageConverters.add(new NullConverter()); // prevent stupid errors

        template.setMessageConverters(messageConverters);

        template.setErrorHandler(new KernelResponseErrorHandler());

        return template;
    }


    /* data core client */
    @Bean
    @Qualifier("dataCore")
    public RestTemplate dataCoreRestTemplate(Gson dataCoreGson) {
        RestTemplate template = new RestTemplate(newRequestFactory());

        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        gsonHttpMessageConverter.setGson(dataCoreGson);
        messageConverters.add(gsonHttpMessageConverter);
        template.setMessageConverters(messageConverters);

        template.setInterceptors(Arrays.asList(datacoreSecurityInterceptor(), new KernelLoggingInterceptor()));

        return template;
    }

    @Bean
    public Gson dataCoreGson() {
        return new GsonBuilder().registerTypeAdapter(DCResource.class, new DCResourceTypeAdapter())
                .registerTypeAdapter(DCRights.class, new DCRightsTypeAdapter())
                .create();
    }


    @Bean
    public ClientHttpRequestInterceptor datacoreSecurityInterceptor() {
        return new DatacoreSecurityInterceptor();
    }

}
