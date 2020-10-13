package com.synopsys.integration.alert.build;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gradle.api.Project;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.options.Option;

public class RunServerTask extends Exec {
    private boolean suspend = false;
    private String postgresVersion;
    private boolean reuseContainer = false;

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

    @Override
    protected void exec() {
        if (null == postgresVersion || postgresVersion.trim().length() == 0) {
            throw new RuntimeException("You must specify a Postgres version to run with.");
        }

        Map<String, String> envVars = new HashMap<>();
        envVars.put("ALERT_ENCRYPTION_PASSWORD", "changeme");
        envVars.put("ALERT_ENCRYPTION_GLOBAL_SALT", "changeme");
        envVars.put("ALERT_TRUST_CERT", "true");
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

    public List<String> getDebugVariables() {
        return List.of(
            "-Xdebug",
            "-Xrunjdwp:transport=dt_socket,server=y,address=9095,suspend=" + (suspend ? "y" : "n")
        );
    }

    public List<String> getJMXVariables(String buildDirectory) {
        return List.of("-Dcom.sun.management.jmxremote",
            "-Dcom.sun.management.jmxremote.port=9045",
            "-Dcom.sun.management.jmxremote.local.only=false",
            "-Dcom.sun.management.jmxremote.authenticate=false",
            "-Dcom.sun.management.jmxremote.ssl=false",
            String.format("-Djavax.net.ssl.trustStore=%s/certs/blackduck-alert.truststore", buildDirectory));
    }

    public List<String> getApplicationVariables(String buildDirectory) {
        // change the --server.ssl.key-store parameter to the keystore file to use for running over ssl
        return List.of(
            String.format("--server.ssl.key-store=%s/certs/blackduck-alert.keystore", buildDirectory),
            String.format("--server.ssl.trust-store=%s/certs/blackduck-alert.truststore", buildDirectory),
            "--server.port=8443",

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
            "--spring.datasource.hikari.jdbc-url=jdbc:postgresql://${embedded.postgresql.host}:${embedded.postgresql.port}/${embedded.postgresql.database}",

            "--hibernate.default_schema=alert",
            "--spring.test.database.replace=none",

            String.format("--alert.images.dir=%s/resources/main/images", buildDirectory),
            String.format("--alert.email.attachments.dir=%s/email/attachments", buildDirectory));
    }

}
