package com.synopsys.integration.alert.util;

import java.util.Optional;

import com.synopsys.integration.alert.common.AlertProperties;

public class TestAlertProperties extends AlertProperties {
    private String alertConfigHome;
    private String alertTemplatesDir;
    private String alertImagesDir;
    private Boolean alertTrustCertificate;
    private String alertProxyHost;
    private String alertProxyPort;
    private String alertProxyUsername;
    private String alertProxyPassword;
    private Boolean sslEnabled = false;

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

    public void setAlertTrustCertificate(final Boolean alertTrustCertificate) {
        this.alertTrustCertificate = alertTrustCertificate;
    }

    @Override
    public Boolean getSslEnabled() {
        return this.sslEnabled;
    }

    public void setSslEnabled(final Boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }
}
