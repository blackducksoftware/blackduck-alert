/*
 * service-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.service.email.model;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.rest.model.ConfigWithMetadata;
import com.synopsys.integration.alert.common.rest.model.Obfuscated;

public class EmailGlobalConfigModel extends ConfigWithMetadata implements Obfuscated<EmailGlobalConfigModel> {
    private String smtpFrom;
    private String smtpHost;
    private Integer smtpPort;

    private Boolean smtpAuth;
    private String smtpUsername;
    private Boolean isSmtpPasswordSet;
    private String smtpPassword;

    private Map<String, String> additionalJavaMailProperties;

    @Override
    public EmailGlobalConfigModel obfuscate() {
        EmailGlobalConfigModel emailGlobalConfigModel = new EmailGlobalConfigModel();

        emailGlobalConfigModel.setId(getId());
        emailGlobalConfigModel.setName(getName());
        emailGlobalConfigModel.setLastUpdated(getLastUpdated());
        emailGlobalConfigModel.setCreatedAt(getCreatedAt());

        emailGlobalConfigModel.setSmtpFrom(smtpFrom);
        emailGlobalConfigModel.setSmtpHost(smtpHost);
        emailGlobalConfigModel.setSmtpPort(smtpPort);
        emailGlobalConfigModel.setSmtpAuth(smtpAuth);
        emailGlobalConfigModel.setSmtpUsername(smtpUsername);
        emailGlobalConfigModel.setAdditionalJavaMailProperties(additionalJavaMailProperties);

        emailGlobalConfigModel.setIsSmtpPasswordSet(StringUtils.isNotBlank(smtpPassword));
        emailGlobalConfigModel.setSmtpPassword(null);

        return emailGlobalConfigModel;
    }

    public Optional<String> getSmtpFrom() {
        return Optional.ofNullable(smtpFrom);
    }

    public void setSmtpFrom(String smtpFrom) {
        this.smtpFrom = smtpFrom;
    }

    public Optional<String> getSmtpHost() {
        return Optional.ofNullable(smtpHost);
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public Optional<Integer> getSmtpPort() {
        return Optional.ofNullable(smtpPort);
    }

    public void setSmtpPort(Integer smtpPort) {
        this.smtpPort = smtpPort;
    }

    public Optional<Boolean> getSmtpAuth() {
        return Optional.ofNullable(smtpAuth);
    }

    public void setSmtpAuth(Boolean smtpAuth) {
        this.smtpAuth = smtpAuth;
    }

    public Optional<String> getSmtpUsername() {
        return Optional.ofNullable(smtpUsername);
    }

    public void setSmtpUsername(String smtpUsername) {
        this.smtpUsername = smtpUsername;
    }

    public Boolean getIsSmtpPasswordSet() {
        return isSmtpPasswordSet;
    }

    public void setIsSmtpPasswordSet(Boolean smtpPasswordSet) {
        isSmtpPasswordSet = smtpPasswordSet;
    }

    public Optional<String> getSmtpPassword() {
        return Optional.ofNullable(smtpPassword);
    }

    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    public Optional<Map<String, String>> getAdditionalJavaMailProperties() {
        return Optional.ofNullable(additionalJavaMailProperties);
    }

    public void setAdditionalJavaMailProperties(Map<String, String> additionalJavaMailProperties) {
        this.additionalJavaMailProperties = additionalJavaMailProperties;
    }

}
