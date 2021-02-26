/*
 * test-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.test.common;

import java.io.IOException;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;

import com.synopsys.integration.alert.common.AlertProperties;

// TODO rename class to include "Mock"
public class TestAlertProperties extends AlertProperties {
    private String alertConfigHome;
    private String alertImagesDir;
    private String alertSecretsDir;
    private Boolean alertTrustCertificate;
    private String alertProxyHost;
    private String alertProxyPort;
    private String alertProxyUsername;
    private String alertProxyPassword;
    private boolean sslEnabled = false;
    private String encryptionPassword;
    private String encryptionSalt;

    public TestAlertProperties() {
        try {
            alertImagesDir = new ClassPathResource("images").getFile().getAbsolutePath();
        } catch (IOException e) {
            alertImagesDir = getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + "../../../../src/main/resources/images/";
        }

        encryptionPassword = "changeme";
        encryptionSalt = "changeme";
        this.alertSecretsDir = "./testDB/run/secrets";
    }

    @Override
    public String getAlertConfigHome() {
        return alertConfigHome;
    }

    public void setAlertConfigHome(String alertConfigHome) {
        this.alertConfigHome = alertConfigHome;
    }

    @Override
    public String getAlertImagesDir() {
        return alertImagesDir;
    }

    public void setAlertImagesDir(String alertImagesDir) {
        this.alertImagesDir = alertImagesDir;
    }

    @Override
    public Optional<Boolean> getAlertTrustCertificate() {
        return Optional.ofNullable(alertTrustCertificate);
    }

    public void setAlertTrustCertificate(Boolean alertTrustCertificate) {
        this.alertTrustCertificate = alertTrustCertificate;
    }

    @Override
    public boolean getSslEnabled() {
        return this.sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    @Override
    public Optional<String> getAlertEncryptionGlobalSalt() {
        return Optional.of(encryptionSalt);
    }

    public void setEncryptionPassword(String encryptionPassword) {
        this.encryptionPassword = encryptionPassword;
    }

    @Override
    public Optional<String> getAlertEncryptionPassword() {
        return Optional.of(encryptionPassword);
    }

    public void setEncryptionSalt(String encryptionSalt) {
        this.encryptionSalt = encryptionSalt;
    }

    @Override
    public String getAlertSecretsDir() {
        return this.alertSecretsDir;
    }

    public void setAlertSecretsDir(String alertSecretsDir) {
        this.alertSecretsDir = alertSecretsDir;
    }

}
