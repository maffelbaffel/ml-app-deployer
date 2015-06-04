package com.marklogic.appdeployer.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.marklogic.appdeployer.command.restapis.CreateRestApiServersCommand;

/**
 * Very simple configuration for when you only want a REST API server created.
 */
@Configuration
public class RestApiConfiguration {

    @Bean
    public CreateRestApiServersCommand restApiPlugin() {
        return new CreateRestApiServersCommand();
    }

}