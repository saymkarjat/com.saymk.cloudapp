package com.saymk.cloud6x;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {


    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {

        return new PostgreSQLContainer<>("postgres:15.3")
                .withDatabaseName("test-db")
                .withUsername("test-user")
                .withPassword("test-pass");
    }


}
