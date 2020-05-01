package com.synopsys.integration.alert.build;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.options.Option;

public class RunServerTask extends Exec {
    private boolean suspend = false;
    private boolean envOverride = false;

    @Option(option = "suspend", description = "Suspends the server until a debug connection is made")
    public void setSuspend(boolean suspend) {
        this.suspend = suspend;
    }

    @Option(option = "envOverride", description = "True if Alert environment variables should override configuration values; False otherwise.")
    public void setEnvOverride(boolean envOverride) {
        this.envOverride = envOverride;
    }

    @Override
    protected void exec() {
        String userHome = System.getProperties().getProperty("user.home");
        String user = new File(userHome).getName();
        Map<String, String> envVars = new HashMap<>();
        envVars.put("ALERT_ENCRYPTION_PASSWORD", "changeme");
        envVars.put("ALERT_ENCRYPTION_GLOBAL_SALT", "changeme");
        envVars.put("ALERT_TRUST_CERT", "true");
        getEnvironment().putAll(envVars);
        super.exec();
    }

    public String[] getDebugVariables() {
        return new String[] {
            "-Xdebug",
            "-Xrunjdwp:transport=dt_socket,server=y,address=9095,suspend=" + (suspend ? "y" : "n")
        };
    }

}
