/*
 * service-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.service.email;

import java.util.Properties;

import com.synopsys.integration.builder.Buildable;

public class SmtpConfig implements Buildable {
    private final Properties javamailProperties;
    private final String smtpFrom;
    private final String smtpHost;
    private final int smtpPort;
    private final boolean smtpAuth;
    private final String smtpUsername;
    private final String smtpPassword;

    public static SmtpConfigBuilder builder() {
        return new SmtpConfigBuilder();
    }

    public SmtpConfig(Properties javamailProperties, String smtpFrom, String smtpHost, int smtpPort, boolean smtpAuth, String smtpUsername, String smtpPassword) {
        this.javamailProperties = javamailProperties;
        this.smtpFrom = smtpFrom;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpAuth = smtpAuth;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
    }

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
}