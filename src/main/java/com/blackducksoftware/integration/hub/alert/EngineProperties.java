package com.blackducksoftware.integration.hub.alert;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class EngineProperties {
    @Value("${blackduck.hub.url}")
    private String hubUrl;

    @Value("${blackduck.hub.timeout}")
    private Integer hubTimeout;

    @Value("${blackduck.hub.username}")
    private String hubUsername;

    @Value("${blackduck.hub.password}")
    private String hubPassword;

    @Value("${blackduck.hub.proxy.host}")
    private String hubProxyHost;

    @Value("${blackduck.hub.proxy.port}")
    private String hubProxyPort;

    @Value("${blackduck.hub.proxy.username}")
    private String hubProxyUsername;

    @Value("${blackduck.hub.proxy.password}")
    private String hubProxyPassword;

    @Value("${blackduck.hub.always.trust.cert}")
    private Boolean hubAlwaysTrustCertificate;

    @Value("${notification.accumulator.cron}")
    private String accumulatorCron;

    public String getHubUrl() {
        return hubUrl;
    }

    public void setHubUrl(final String hubUrl) {
        this.hubUrl = hubUrl;
    }

    public Integer getHubTimeout() {
        return hubTimeout;
    }

    public void setHubTimeout(final Integer hubTimeout) {
        this.hubTimeout = hubTimeout;
    }

    public String getHubUsername() {
        return hubUsername;
    }

    public void setHubUsername(final String hubUsername) {
        this.hubUsername = hubUsername;
    }

    public String getHubPassword() {
        return hubPassword;
    }

    public void setHubPassword(final String hubPassword) {
        this.hubPassword = hubPassword;
    }

    public String getHubProxyHost() {
        return hubProxyHost;
    }

    public void setHubProxyHost(final String hubProxyHost) {
        this.hubProxyHost = hubProxyHost;
    }

    public String getHubProxyPort() {
        return hubProxyPort;
    }

    public void setHubProxyPort(final String hubProxyPort) {
        this.hubProxyPort = hubProxyPort;
    }

    public String getHubProxyUsername() {
        return hubProxyUsername;
    }

    public void setHubProxyUsername(final String hubProxyUsername) {
        this.hubProxyUsername = hubProxyUsername;
    }

    public String getHubProxyPassword() {
        return hubProxyPassword;
    }

    public void setHubProxyPassword(final String hubProxyPassword) {
        this.hubProxyPassword = hubProxyPassword;
    }

    public Boolean getHubAlwaysTrustCertificate() {
        return hubAlwaysTrustCertificate;
    }

    public void setHubAlwaysTrustCertificate(final Boolean hubAlwaysTrustCertificate) {
        this.hubAlwaysTrustCertificate = hubAlwaysTrustCertificate;
    }

    public String getAccumulatorCron() {
        return accumulatorCron;
    }

    public void setAccumulatorCron(final String accumulatorCron) {
        this.accumulatorCron = accumulatorCron;
    }
}
