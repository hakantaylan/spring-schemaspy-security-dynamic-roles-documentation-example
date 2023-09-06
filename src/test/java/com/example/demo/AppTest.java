package com.example.demo;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

public class AppTest {

    public static void main(String[] args) {

        SpringApplication.from(DemoApplication::main)
                .with(TestContainersConfiguration.class)
                .run(args);
    }

    @TestConfiguration(proxyBeanMethods = false)
    public static class TestContainersConfiguration {

        @Bean
        @ServiceConnection
//        @RestartScope
        public PostgreSQLContainer<?> postgreSQLContainer() {
            return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15.1"))
                    .withUsername("testUser")
                    .withPassword("testSecret")
                    .withDatabaseName("testDatabase")
                    .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("testcontainers.postgres")));
        }
    }
}
