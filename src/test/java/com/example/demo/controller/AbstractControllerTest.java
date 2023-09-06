package com.example.demo.controller;

import org.junit.jupiter.api.BeforeAll;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class AbstractControllerTest {

    public static final Network NETWORK = Network.newNetwork();
    @ServiceConnection
    @Container
//    @RestartScope
    public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15.1"))
//            .withUsername("testUser")
//            .withPassword("testSecret")
//            .withDatabaseName("testDatabase")
            .withNetworkAliases("postgres")
            .withNetwork(NETWORK)
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("testcontainers.postgres")));

    @BeforeAll
    static void setup(){
        Startables.deepStart(POSTGRES).join();
    }
}
