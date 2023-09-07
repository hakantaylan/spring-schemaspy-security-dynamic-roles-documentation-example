package com.example.demo.controller;


import java.nio.file.Files;
import java.nio.file.Path;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

class SchemaSpyTest extends AbstractControllerTest {

  @Test
  @SneakyThrows
  void schemaSpy() {
    @Cleanup
    final var schemaSpy =
        new GenericContainer<>(DockerImageName.parse("schemaspy/schemaspy:6.1.0"))
            .withNetworkAliases("schemaspy")
            .withNetwork(NETWORK)
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("SchemaSpy")))
            .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint("").withTty(true))
            .withCommand("sleep 500000");

    schemaSpy.start();

    schemaSpy.execInContainer("rm", "/drivers_inc/postgresql-42.1.1.jre7.jar");
//    schemaSpy.copyFileToContainer(MountableFile.forHostPath(Path.of(getClass().getResource("/postgresql-42.6.0.jar").toURI())), "/drivers_inc/");
    schemaSpy.copyFileToContainer(MountableFile.forClasspathResource("postgresql-42.6.0.jar"), "/drivers_inc/");
    Container.ExecResult ls = schemaSpy.execInContainer("ls", "drivers_inc");
    System.out.println(ls.getStdout());
    schemaSpy.execInContainer(
            "java",
            "-jar", "/schemaspy-6.1.0.jar",
            "-t", "pgsql",    // postgre13 için pgsql11 yazmak gerekiyor
            "-db", POSTGRES.getDatabaseName(),
            "-host", "postgres",
            "-u", POSTGRES.getUsername(),
            "-p", POSTGRES.getPassword(),
            "-o", "/output",
            "-dp", "/drivers_inc",
            "-debug"
    );

    schemaSpy.execInContainer("tar", "-czvf", "/tmp/output.tar.gz", "/output");
    ls = schemaSpy.execInContainer("ls", "/tmp/");
    System.out.println(ls.getStdout());
    final var buildFolderPath =
        Path.of(getClass().getResource("/").toURI().resolve("../docs")).toAbsolutePath();
    schemaSpy.copyFileFromContainer(
        "/tmp/output.tar.gz",
        buildFolderPath.resolve("output.tar.gz").toString()
    );
    schemaSpy.stop();

    final var archiver = ArchiverFactory.createArchiver("tar", "gz");
    archiver.extract(buildFolderPath.resolve("output.tar.gz").toFile(),
        buildFolderPath.toFile());

    Files.writeString(
        buildFolderPath.resolve("index.html"),
        """
            <html>
            <head>
                <meta charset="UTF8">
                <style>
                    body, table {
                        font-family: "JetBrains Mono";
                        font-size: 20px;
                    }
                    table, th, td {
                      border: 1px solid black;
                    }
                </style>
                <link href='https://fonts.googleapis.com/css?family=JetBrains Mono' rel='stylesheet'>
            </head>
            <body>
                <div>
                    <h3><a href="security-docs">Endpoints role checking</a></h3>
                    <h3><a href="output">Schema Spy</a></h3>
                </div>
            </body>
            </html>"""
    );
  }
}
