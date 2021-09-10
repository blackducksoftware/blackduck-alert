/*
 * api-channel-jira
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution.custom;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class JiraCustomFieldReplacementValues {
    // "None" is a frequently used default String for many Jira custom-fields
    public static final String DEFAULT_REPLACEMENT = "None";

    private final String providerName;
    private final String projectName;
    private final String projectVersionName;
    private final String componentName;
    private final String componentVersionName;
    private final String severity;

    public static JiraCustomFieldReplacementValues trivial(LinkableItem provider) {
        return new JiraCustomFieldReplacementValues(provider.getLabel(), DEFAULT_REPLACEMENT, null, null, null, null);
    }

    public JiraCustomFieldReplacementValues(
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
