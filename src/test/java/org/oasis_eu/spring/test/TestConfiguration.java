package org.oasis_eu.spring.test;

import org.oasis_eu.spring.config.KernelConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * User: schambon
 * Date: 4/24/14
 */
@Configuration
@Import(KernelConfiguration.class)
@PropertySource("classpath:test-configuration.properties")
public class TestConfiguration {


    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
