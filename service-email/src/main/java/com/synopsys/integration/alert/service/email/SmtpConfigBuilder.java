/*
 * service-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.service.email;

import java.util.Properties;

import com.synopsys.integration.builder.BuilderStatus;
import com.synopsys.integration.builder.IntegrationBuilder;

public class SmtpConfigBuilder extends IntegrationBuilder<SmtpConfig> {
    private Properties javamailProperties;
    private String smtpFrom;
    private String smtpHost;
    private int smtpPort;
    private boolean smtpAuth;
    private String smtpUsername;
    private String smtpPassword;

    public Properties getJavamailProperties() {
        return javamailProperties;
    }

    public String getSmtpFrom() {
        return smtpFrom;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public boolean isSmtpAuth() {
        return smtpAuth;
    }

    public String getSmtpUsername() {
        return smtpUsername;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public SmtpConfigBuilder setJavamailProperties(Properties javamailProperties) {
        this.javamailProperties = javamailProperties;
        return this;
    }

    public SmtpConfigBuilder setSmtpFrom(String smtpFrom) {
        this.smtpFrom = smtpFrom;
        return this;
    }

    public SmtpConfigBuilder setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
        return this;
    }

    public SmtpConfigBuilder setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
        return this;
    }

    public SmtpConfigBuilder setSmtpAuth(boolean smtpAuth) {
        this.smtpAuth = smtpAuth;
        return this;
    }

    public SmtpConfigBuilder setSmtpUsername(String smtpUsername) {
        this.smtpUsername = smtpUsername;
        return this;
    }

    public SmtpConfigBuilder setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
        return this;
    }

    @Override
    protected SmtpConfig buildWithoutValidation() {
        return new SmtpConfig(javamailProperties, smtpFrom, smtpHost, smtpPort, smtpAuth, smtpUsername, smtpPassword);
    }

    @Override
    protected void validate(BuilderStatus builderStatus) {
        // No validation on this class yet
    }
}