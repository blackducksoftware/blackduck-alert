/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.test.common;

import java.nio.file.Path;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.AlertProperties;

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
    private String alertEmailAttachmentsDir;

    private String alertTrustStoreFile;
    private Long loginLockoutThreshold = 10L;
    private Long loginLockoutMinutes = 10L;

    private Integer notificationMappingBatchLimit;

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
    public String createBlackDuckLogoPath() throws AlertException {
        String imagesDirectory = getAlertImagesDir();
        if (StringUtils.isNotBlank(imagesDirectory)) {
            return Path.of(imagesDirectory, "BlackDuck_test_logo.png").toString();
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

    @Override
    public String getAlertEmailAttachmentsDir() {
        return this.alertEmailAttachmentsDir;
    }

    public void setAlertEmailAttachmentsDir(String alertEmailAttachmentsDir) {
        this.alertEmailAttachmentsDir = alertEmailAttachmentsDir;
    }

    @Override
    public Optional<String> getTrustStoreFile() {
        if (alertTrustStoreFile == null) {
            return super.getTrustStoreFile();
        }
        return Optional.ofNullable(alertTrustStoreFile);
    }

    @Override
    public Long getLoginLockoutThreshold() {
        return this.loginLockoutThreshold;
    }

    public void setLoginLockoutThreshold(Long loginLockoutThreshold) {
        this.loginLockoutThreshold = loginLockoutThreshold;
    }

    @Override
    public Long getLoginLockoutMinutes() {
        return this.loginLockoutMinutes;
    }

    public void setLoginLockoutMinutes(Long loginLockoutMinutes) {
        this.loginLockoutMinutes = loginLockoutMinutes;
    }

    public void setTrustStoreFile(String trustStoreFile) {
        this.alertTrustStoreFile = trustStoreFile;
    }

    @Override
    public Optional<Integer> getNotificationMappingBatchLimit() {
        return Optional.ofNullable(notificationMappingBatchLimit);
    }

    public void setNotificationMappingBatchLimit(Integer notificationMappingBatchLimit) {
        this.notificationMappingBatchLimit = notificationMappingBatchLimit;
    }

    private Path computeImagesDirPath() {
        ProtectionDomain protectionDomain = getClass().getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        String codeSourcePath = codeSource.getLocation().getPath();
        String projectRootPath = StringUtils.substringBeforeLast(codeSourcePath, SUB_PROJECT_BUILD_DIR_IDENTIFIER);
        return Path.of(projectRootPath, SUB_PROJECT_DIR_NAME, RESOURCE_DIR, IMAGES_DIR);
    }

}
