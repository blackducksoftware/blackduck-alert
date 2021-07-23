/*
 * api-channel-jira
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution.custom;

import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

public class MessageReplacementValues {
    public final static String DEFAULT_NOTIFICATION_REPLACEMENT_VALUE = "None";

    private final String providerName;
    private final String projectName;
    private final String projectVersionName;
    private final String componentName;
    private final String componentVersionName;
    private final String severity;

    public static MessageReplacementValues trivial(String providerLabel, String projectName) {
        return new MessageReplacementValues(
            providerLabel,
            projectName,
            MessageReplacementValues.DEFAULT_NOTIFICATION_REPLACEMENT_VALUE,
            MessageReplacementValues.DEFAULT_NOTIFICATION_REPLACEMENT_VALUE,
            MessageReplacementValues.DEFAULT_NOTIFICATION_REPLACEMENT_VALUE,
            MessageReplacementValues.DEFAULT_NOTIFICATION_REPLACEMENT_VALUE
        );
    }

    public MessageReplacementValues(
        String providerName,
        String projectName,
        @Nullable String projectVersionName,
        @Nullable String componentName,
        @Nullable String componentVersionName,
        @Nullable String severity
    ) {
        this.providerName = providerName;
        this.projectName = projectName;
        this.projectVersionName = StringUtils.trimToNull(projectVersionName);
        this.componentName = StringUtils.trimToNull(componentName);
        this.componentVersionName = StringUtils.trimToNull(componentVersionName);
        this.severity = StringUtils.trimToNull(severity);
    }

    public String getProviderName() {
        return providerName;
    }

    public String getProjectName() {
        return projectName;
    }

    public Optional<String> getProjectVersionName() {
        return Optional.ofNullable(projectVersionName);
    }

    public Optional<String> getComponentName() {
        return Optional.ofNullable(componentName);
    }

    public Optional<String> getComponentVersionName() {
        return Optional.ofNullable(componentVersionName);
    }

    public Optional<String> getSeverity() {
        return Optional.ofNullable(severity);
    }

}
