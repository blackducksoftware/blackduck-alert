package com.synopsys.integration.alert.util;

import java.util.Optional;

import com.synopsys.integration.alert.common.AlertProperties;

public class TestAlertProperties extends AlertProperties {
    public static final String PROPERTY_USER_DIR = "user.dir";
    public static final String RESOURCES_PATH = "/src/main/resources";
    public static final String IMAGES_PATH = RESOURCES_PATH + "/images/";

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
        String userDirectory = System.getProperties().getProperty(PROPERTY_USER_DIR);
        alertImagesDir = userDirectory + IMAGES_PATH;

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
