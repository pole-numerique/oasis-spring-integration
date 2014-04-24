package org.oasis.spring.test;

import org.oasis.spring.config.KernelConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ResourceLoader;

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
