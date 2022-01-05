/*
 * test-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.test.common;

import java.nio.file.Path;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.AlertProperties;

public class MockAlertProperties extends AlertProperties {
    private static final String SUB_PROJECT_DIR_NAME = "test-common";
    private static final String SUB_PROJECT_BUILD_DIR_IDENTIFIER = Path.of(SUB_PROJECT_DIR_NAME, "build").toString();
    private static final String RESOURCE_DIR = "src/main/resources";
    private static final String IMAGES_DIR = "images";

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

    public MockAlertProperties() {
        alertImagesDir = computeImagesDirPath().toString();

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

    @Override
    public String createSynopsysLogoPath() throws AlertException {
        String imagesDirectory = getAlertImagesDir();
        if (StringUtils.isNotBlank(imagesDirectory)) {
            return Path.of(imagesDirectory, "test_logo.png").toString();
        }
        throw new AlertException(String.format("Could not find the Alert logo in the images directory '%s'", imagesDirectory));
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

    private Path computeImagesDirPath() {
        ProtectionDomain protectionDomain = getClass().getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        String codeSourcePath = codeSource.getLocation().getPath();
        String projectRootPath = StringUtils.substringBeforeLast(codeSourcePath, SUB_PROJECT_BUILD_DIR_IDENTIFIER);
        return Path.of(projectRootPath, SUB_PROJECT_DIR_NAME, RESOURCE_DIR, IMAGES_DIR);
    }

}
