package com.blackducksoftware.integration.alert;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.alert.common.AlertProperties;

public class TestAlertProperties extends AlertProperties {
    private String alertConfigHome;
    private String alertTemplatesDir;
    private String alertImagesDir;
    private Boolean alertTrustCertificate;
    private String alertProxyHost;
    private String alertProxyPort;
    private String alertProxyUsername;
    private String alertProxyPassword;

    public TestAlertProperties() {
    }

    @Override
    public String getAlertConfigHome() {
        return alertConfigHome;
    }

    public void setAlertConfigHome(final String alertConfigHome) {
        this.alertConfigHome = alertConfigHome;
    }

    @Override
    public String getAlertTemplatesDir() {
        return alertTemplatesDir;
    }

    public void setAlertTemplatesDir(final String alertTemplatesDir) {
        this.alertTemplatesDir = alertTemplatesDir;
    }

    @Override
    public String getAlertImagesDir() {
        return alertImagesDir;
    }

    public void setAlertImagesDir(final String alertImagesDir) {
        this.alertImagesDir = alertImagesDir;
    }

    @Override
    public Optional<Boolean> getAlertTrustCertificate() {
        return Optional.ofNullable(alertTrustCertificate);
    }

    @Override
    public Optional<String> getAlertProxyHost() {
        if (StringUtils.isNotBlank(alertProxyHost)) {
            return Optional.of(alertProxyHost);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getAlertProxyPort() {
        if (StringUtils.isNotBlank(alertProxyPort)) {
            return Optional.of(alertProxyPort);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getAlertProxyUsername() {
        if (StringUtils.isNotBlank(alertProxyUsername)) {
            return Optional.of(alertProxyUsername);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getAlertProxyPassword() {
        if (StringUtils.isNotBlank(alertProxyPassword)) {
            return Optional.of(alertProxyPassword);
        }
        return Optional.empty();
    }

    public void setAlertTrustCertificate(final Boolean alertTrustCertificate) {
        this.alertTrustCertificate = alertTrustCertificate;
    }

    public void setAlertProxyHost(final String alertProxyHost) {
        this.alertProxyHost = alertProxyHost;
    }

    public void setAlertProxyPort(final String alertProxyPort) {
        this.alertProxyPort = alertProxyPort;
    }

    public void setAlertProxyUsername(final String alertProxyUsername) {
        this.alertProxyUsername = alertProxyUsername;
    }

    public void setAlertProxyPassword(final String alertProxyPassword) {
        this.alertProxyPassword = alertProxyPassword;
    }
}
