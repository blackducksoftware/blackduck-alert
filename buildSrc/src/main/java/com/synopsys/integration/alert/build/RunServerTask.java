package com.synopsys.integration.alert.build;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.tasks.Exec;

public class RunServerTask extends Exec {
    @Override
    protected void exec() {
        String userHome = System.getProperties().getProperty("user.home");
        String user = new File(userHome).getName();
        Map<String, String> envVars = new HashMap<>();
        envVars.put("ALERT_COMPONENT_SETTINGS_SETTINGS_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE", "true");
        envVars.put("ALERT_COMPONENT_SETTINGS_SETTINGS_USER_DEFAULT_ADMIN_EMAIL", String.format("%s@synopsys.com", user));
        envVars.put("ALERT_ENCRYPTION_PASSWORD", "changeme");
        envVars.put("ALERT_ENCRYPTION_GLOBAL_SALT", "changeme");
        envVars.put("ALERT_TRUST_CERT", "true");
        getEnvironment().putAll(envVars);
        super.exec();
    }

}
