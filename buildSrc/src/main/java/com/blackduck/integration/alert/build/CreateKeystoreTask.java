/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.build;

import java.util.List;

import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.Internal;

public class CreateKeystoreTask extends Exec {

    @Override
    protected void exec() {
        String workingDir = getProject().getBuildDir().getAbsolutePath() + "/certs";
        setWorkingDir(workingDir);
        commandLine(getKeytoolVariables());
        super.exec();
    }

    @Internal
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
            "365",
            "-storepass",
            "changeit",
            "-dname",
            "CN=localhost, OU=Engineering, O=Black Duck, C=US",
            "-ext",
            "eku=sa,ca",
            "-ext",
            "BasicConstraints=ca:true",
            "-ext",
            "san=dns:localhost,dns:localhost.localdomain,dns:lvh.me,ip:127.0.0.1,ip:FE80:0:0:0:0:0:0:1"
        );
    }
}
