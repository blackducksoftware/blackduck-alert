package com.synopsys.integration.alert.build;

import java.util.List;

import org.gradle.api.tasks.Exec;

public class CreateKeystoreTask extends Exec {

    @Override
    protected void exec() {
        String workingDir = getProject().getBuildDir().getAbsolutePath() + "/certs";
        setWorkingDir(workingDir);
        commandLine(getKeytoolVariables());
        super.exec();
    }

    public List<String> getKeytoolVariables() {
        return List.of(
            "keytool",
            "-genkeypair",
            "-v",
            "-keystore",
            "blackduck-alert.keystore",
            "-alias",
            "blackduck-alert",
            "-keyalg",
            "RSA",
            "-keysize",
            "2048",
            "-storetype",
            "PKCS12",
            "-validity",
            "3650",
            "-storepass",
            "changeit",
            "-dname",
            "CN=localhost, OU=Engineering, O=Synopsys, C=US"
        );
    }
}
