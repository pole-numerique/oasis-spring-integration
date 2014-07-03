package org.oasis_eu.spring.kernel.dao;

import org.oasis_eu.spring.config.KernelConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * User: schambon
 * Date: 6/13/14
 */
@Configuration
@Import(KernelConfiguration.class)
@ComponentScan
@PropertySource("classpath:test-configuration.properties")
public class DAOTestConfiguration {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
