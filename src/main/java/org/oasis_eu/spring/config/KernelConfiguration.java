package org.oasis_eu.spring.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.oasis_eu.spring.datacore.impl.DCResourceTypeAdapter;
import org.oasis_eu.spring.datacore.impl.DCRightsTypeAdapter;
import org.oasis_eu.spring.datacore.impl.DatacoreSecurityInterceptor;
import org.oasis_eu.spring.datacore.impl.GsonMessageConverter;
import org.oasis_eu.spring.datacore.model.DCResource;
import org.oasis_eu.spring.datacore.model.DCRights;
import org.oasis_eu.spring.kernel.rest.KernelResponseErrorHandler;
import org.oasis_eu.spring.kernel.security.OpenIdCConfiguration;
import org.oasis_eu.spring.kernel.security.StaticOpenIdCConfiguration;
import org.oasis_eu.spring.util.KernelLoggingInterceptor;
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
@ComponentScan(basePackages = {"org.oasis_eu.spring.kernel", "org.oasis_eu.spring.datacore"})
public class KernelConfiguration {


    @Bean
    public OpenIdCConfiguration openIdCConfiguration() {
        OpenIdCConfiguration configuration = new StaticOpenIdCConfiguration();
        return configuration;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JSR310Module());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

    @Bean
    @Qualifier("kernelRestTemplate")
    public RestTemplate kernelRestTemplate() {
        RestTemplate template = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

        template.setInterceptors(Arrays.asList(new KernelLoggingInterceptor()));

        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();

        messageConverters.add(new FormHttpMessageConverter());
        messageConverters.add(new StringHttpMessageConverter());

        MappingJackson2HttpMessageConverter jacksonMessageConverter = new MappingJackson2HttpMessageConverter();
        jacksonMessageConverter.setObjectMapper(objectMapper());
        messageConverters.add(jacksonMessageConverter);

        template.setMessageConverters(messageConverters);

        template.setErrorHandler(new KernelResponseErrorHandler());

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
                .create();
    }



    @Bean
    public ClientHttpRequestInterceptor datacoreSecurityInterceptor() {
        return new DatacoreSecurityInterceptor();
    }

}
