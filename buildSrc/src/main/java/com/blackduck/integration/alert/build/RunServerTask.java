/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.build;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.gradle.api.Project;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.options.Option;

public class RunServerTask extends Exec {
    private boolean suspend = false;
    private String postgresVersion;
    private boolean reuseContainer = false;
    private String encryptionPassword = "changeme";
    private String encryptionSalt = "changeme";
    private boolean externalDb = false;
    private String externalDbHost = "localhost";
    private String externalDbPort = "5432";
    private String externalDBName = "alertdb";
    private String externalDbUser = "sa";
    private String externalDbPassword = "blackduck";
    private boolean externalRabbitmq = false;
    private String externalRabbitHost = "localhost";
    private String externalRabbitPort = "5672";
    private String externalRabbitUser = "sysadmin";
    private String externalRabbitPassword = "blackduck";
    private String externalRabbitVirtualHost = "blackduck-alert";
    private boolean profiler = false;

    @Option(option = "suspend", description = "Suspends the server until a debug connection is made")
    public void setSuspend(boolean suspend) {
        this.suspend = suspend;
    }

    @Option(option = "reuseContainer", description = "Will reuse the database container.")
    public void setReuseContainer(boolean reuseContainer) {
        this.reuseContainer = reuseContainer;
    }

    public void setPostgresVersion(String postgresVersion) {
        this.postgresVersion = postgresVersion;
    }

    @Option(option = "encryption-password", description = "The encryption password to use.")
    public void setEncryptionPassword(String encryptionPassword) {
        this.encryptionPassword = encryptionPassword;
    }

    @Option(option = "encryption-salt", description = "The encryption salt to use.")
    public void setEncryptionSalt(String encryptionSalt) {
        this.encryptionSalt = encryptionSalt;
    }

    @Option(option = "externaldb", description = "Flag to indicate an external database is used")
    public void setExternalDb(boolean externalDb) {
        this.externalDb = externalDb;
    }

    @Option(option = "externaldb-host", description = "Hostname of the external database")
    public void setExternalDbHost(String externalDbHost) {
        this.externalDbHost = externalDbHost;
    }

    @Option(option = "externaldb-port", description = "Port of the external database")
    public void setExternalDbPort(String externalDbPort) {
        this.externalDbPort = externalDbPort;
    }

    @Option(option = "externaldb-name", description = "Database name of the external database")
    public void setExternalDBName(String externalDBName) {
        this.externalDBName = externalDBName;
    }

    @Option(option = "externaldb-user", description = "Name of the user with access to the external database")
    public void setExternalDbUser(String externalDbUser) {
        this.externalDbUser = externalDbUser;
    }

    @Option(option = "externaldb-password", description = "Password of the user with access to the external database")
    public void setExternalDbPassword(String externalDbPassword) {
        this.externalDbPassword = externalDbPassword;
    }

    @Option(option = "external-rabbit", description = "Flag to indicate an external rabbitmq is used")
    public void setExternalRabbitmq(boolean externalRabbitmq) {
        this.externalRabbitmq = externalRabbitmq;
    }

    @Option(option = "external-rabbit-host", description = "Host name of the host where rabbit mq is installed")
    public void setExternalRabbitHost(String externalRabbitHost) {
        this.externalRabbitHost = externalRabbitHost;
    }

    @Option(option = "external-rabbit-port", description = "Port of the external rabbitmq")
    public void setExternalRabbitPort(String externalRabbitPort) {
        this.externalRabbitPort = externalRabbitPort;
    }

    @Option(option = "external-rabbit-user", description = "Username to authenticate with rabbitmq")
    public void setExternalRabbitUser(String externalRabbitUser) {
        this.externalRabbitUser = externalRabbitUser;
    }

    @Option(option = "external-rabbit-password", description = "Password to authenticate with rabbitmq")
    public void setExternalRabbitPassword(String externalRabbitPassword) {
        this.externalRabbitPassword = externalRabbitPassword;
    }

    @Option(option = "external-rabbit-virtual-host", description = "Virtual host name with rabbitmq")
    public void setExternalRabbitVirtualHost(String externalRabbitVirtualHost) {
        this.externalRabbitVirtualHost = externalRabbitVirtualHost;
    }

    @Option(option = "profiler", description = "Adds options to attach a local profiler to the process")
    public void setProfiler(boolean profiler) {
        this.profiler = profiler;
    }


    @Override
    protected void exec() {
        if (null == postgresVersion || postgresVersion.trim().length() == 0) {
            throw new RuntimeException("You must specify a Postgres version to run with.");
        }

        Map<String, String> envVars = new HashMap<>();
        envVars.put("ALERT_ENCRYPTION_PASSWORD", encryptionPassword);
        envVars.put("ALERT_ENCRYPTION_GLOBAL_SALT", encryptionSalt);
        envVars.put("ALERT_TRUST_CERT", "true");
        envVars.put("ALERT_LOG_FILE_PATH", "log");
        getEnvironment().putAll(envVars);

        Project project = getProject();
        String buildDirectory = project.getBuildDir().getAbsolutePath();
        String version = (String) project.getVersion();

        File jarFile = new File(String.format("%s/libs/blackduck-alert-%s.jar", buildDirectory, version));

        List<String> commandArray = new ArrayList();
        commandArray.add("java");
        commandArray.addAll(getDebugVariables());
        commandArray.addAll(getJMXVariables(buildDirectory));
        commandArray.add("-jar");
        commandArray.add(jarFile.getAbsolutePath());
        commandArray.addAll(getApplicationVariables(buildDirectory));
        commandLine(commandArray);
        super.exec();
    }

    @Internal
    public List<String> getDebugVariables() {
        List<String> debugVariables = new ArrayList<>();
        debugVariables.add("-Xdebug");
        debugVariables.add("-Xrunjdwp:transport=dt_socket,server=y,address=9095,suspend=" + (suspend ? "y" : "n"));

        // add profiling options
        if (profiler) {
            debugVariables.add("-XX:+UnlockDiagnosticVMOptions");
            debugVariables.add("-XX:+DebugNonSafepoints");
        }
        return debugVariables;
    }

    public List<String> getJMXVariables(String buildDirectory) {
        return List.of(
            "-Dcom.sun.management.jmxremote",
            "-Dcom.sun.management.jmxremote.port=9045",
            "-Dcom.sun.management.jmxremote.local.only=false",
            "-Dcom.sun.management.jmxremote.authenticate=false",
            "-Dcom.sun.management.jmxremote.ssl=false",
            String.format("-Djavax.net.ssl.trustStore=%s/certs/blackduck-alert.truststore", buildDirectory)
        );
    }

    public List<String> getApplicationVariables(String buildDirectory) {
        // change the --server.ssl.key-store parameter to the keystore file to use for running over ssl
        List<String> variables = new LinkedList<>();
        List<String> commonVariables = List.of(
            String.format("--server.ssl.key-store=%s/certs/blackduck-alert.keystore", buildDirectory),
            String.format("--server.ssl.trust-store=%s/certs/blackduck-alert.truststore", buildDirectory),
            "--server.port=8443",

            "--hibernate.default_schema=alert",
            "--spring.test.database.replace=none",

            String.format("--alert.images.dir=%s/resources/main/images", buildDirectory),
            String.format("--alert.email.attachments.dir=%s/email/attachments", buildDirectory)
        );
        List<String> databaseVariables = getDatabaseVariables();
        List<String> messageQueueVariables = getMessageQueueVariables();
        variables.addAll(commonVariables);
        variables.addAll(databaseVariables);
        variables.addAll(messageQueueVariables);

        return variables;
    }

    @Internal
    public List<String> getDatabaseVariables() {
        if (externalDb) {
            return List.of(
                String.format("--spring.datasource.username=%s", externalDbUser),
                String.format("--spring.datasource.password=%s", externalDbPassword),
                String.format("--spring.datasource.url=jdbc:postgresql://%s:%s/%s", externalDbHost, externalDbPort, externalDBName),
                String.format("--spring.datasource.hikari.jdbc-url=jdbc:postgresql://%s:%s/%s", externalDbHost, externalDbPort, externalDBName)
            );
        }
        // Using embedded Test Containers database
        return List.of(
            // Spring Boot Test Containers https://github.com/testcontainers/testcontainers-spring-boot
            "--embedded.postgresql.enabled=true",
            "--embedded.postgresql.dockerImage=postgres:" + postgresVersion,
            "--embedded.postgresql.reuseContainer=" + reuseContainer,
            "--embedded.postgresql.waitTimeoutInSeconds=20",
            "--embedded.containers.forceShutdown=true",

            // Note: A logging bug in testcontainers causes this to be logged as "embedded.postgresql.schema={value}". Ignore it.
            "--embedded.postgresql.database=alertdb",
            "--embedded.postgresql.user=test",
            "--embedded.postgresql.password=test",
            "--embedded.postgresql.initScriptPath=file:buildSrc/src/main/resources/init_test_db.sql",

            "--spring.datasource.username=sa",
            "--spring.datasource.password=blackduck",
            "--spring.datasource.url=jdbc:postgresql://${embedded.postgresql.host}:${embedded.postgresql.port}/${embedded.postgresql.database}",
            "--spring.datasource.hikari.jdbc-url=jdbc:postgresql://${embedded.postgresql.host}:${embedded.postgresql.port}/${embedded.postgresql.database}"
        );
    }

    @Internal
    public List<String> getMessageQueueVariables() {
        if (externalRabbitmq) {
            return List.of(
                String.format("--spring.rabbitmq.host=%s", externalRabbitHost),
                String.format("--spring.rabbitmq.port=%s", externalRabbitPort),
                String.format("--spring.rabbitmq.username=%s", externalRabbitUser),
                String.format("--spring.rabbitmq.password=%s", externalRabbitPassword),
                String.format("--spring.rabbitmq.virtual-host=%s", externalRabbitVirtualHost)
            );
        }
        return List.of(
            "--embedded.rabbitmq.password=blackduck",
            "--embedded.rabbitmq.vhost=blackduck-alert",

            "--spring.rabbitmq.host=${embedded.rabbitmq.host}",
            "--spring.rabbitmq.port=${embedded.rabbitmq.port}",
            "--spring.rabbitmq.username=${embedded.rabbitmq.user}",
            "--spring.rabbitmq.password=${embedded.rabbitmq.password}",
            "--spring.rabbitmq.virtual-host=${embedded.rabbitmq.vhost}"
        );
    }
}
