/*
 * service-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.service.email.model;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.alert.api.common.model.Obfuscated;
import com.blackduck.integration.alert.common.rest.model.ConfigWithMetadata;

public class EmailGlobalConfigModel extends ConfigWithMetadata implements Obfuscated<EmailGlobalConfigModel> {
    private String smtpFrom;
    private String smtpHost;
    private Integer smtpPort;

    private Boolean smtpAuth;
    private String smtpUsername;
    private Boolean isSmtpPasswordSet;
    private String smtpPassword;

    private Map<String, String> additionalJavaMailProperties;

    public EmailGlobalConfigModel() {
        // For serialization
    }

    public EmailGlobalConfigModel(String id, String name, String smtpFrom, String smtpHost) {
        super(id, name);
        this.smtpFrom = smtpFrom;
        this.smtpHost = smtpHost;
    }

    public EmailGlobalConfigModel(
        String id,
        String name,
        String createdAt,
        String lastUpdated,
        String smtpFrom,
        String smtpHost,
        Integer smtpPort,
        Boolean smtpAuth,
        String smtpUsername,
        String smtpPassword,
        Boolean isSmtpPasswordSet,
        Map<String, String> additionalJavaMailProperties
    ) {
        this(id, name, smtpFrom, smtpHost);
        this.smtpPort = smtpPort;
        this.smtpAuth = smtpAuth;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.isSmtpPasswordSet = isSmtpPasswordSet;
        this.additionalJavaMailProperties = additionalJavaMailProperties;
        setCreatedAt(createdAt);
        setLastUpdated(lastUpdated);
    }

    @Override
    public EmailGlobalConfigModel obfuscate() {
        return new EmailGlobalConfigModel(
            getId(),
            getName(),
            getCreatedAt(),
            getLastUpdated(),
            getSmtpFrom(),
            getSmtpHost(),
            smtpPort,
            smtpAuth,
            smtpUsername,
            null,
            StringUtils.isNotBlank(smtpPassword),
            additionalJavaMailProperties
        );
    }

    public String getSmtpFrom() {
        return smtpFrom;
    }

    public String getSmtpHost() {
        return smtpHost;
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
