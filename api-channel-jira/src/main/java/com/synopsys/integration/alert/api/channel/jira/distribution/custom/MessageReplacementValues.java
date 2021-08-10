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
    private final String componentUsage;
    private final String componentLicense;
    private final String severity;
    private final String policyCategory;

    private MessageReplacementValues(
        String providerName,
        String projectName,
        @Nullable String projectVersionName,
        @Nullable String componentName,
        @Nullable String componentVersionName,
        @Nullable String componentUsage,
        @Nullable String componentLicense,
        @Nullable String severity,
        @Nullable String policyCategory
    ) {
        this.providerName = providerName;
        this.projectName = projectName;
        this.projectVersionName = StringUtils.trimToNull(projectVersionName);
        this.componentName = StringUtils.trimToNull(componentName);
        this.componentVersionName = StringUtils.trimToNull(componentVersionName);
        this.componentUsage = StringUtils.trimToNull(componentUsage);
        this.componentLicense = StringUtils.trimToNull(componentLicense);
        this.severity = StringUtils.trimToNull(severity);
        this.policyCategory = StringUtils.trimToNull(policyCategory);
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

    public Optional<String> getComponentUsage() {
        return Optional.ofNullable(componentUsage);
    }

    public Optional<String> getComponentLicense() {
        return Optional.ofNullable(componentLicense);
    }

    public Optional<String> getSeverity() {
        return Optional.ofNullable(severity);
    }

    public Optional<String> getPolicyCategory() {
        return Optional.ofNullable(policyCategory);
    }

    public static class Builder {
        private String providerName;
        private String projectName;
        private String projectVersionName;
        private String componentName;
        private String componentVersionName;
        private String componentUsage;
        private String componentLicense;
        private String severity;
        private String policyCategory;

        public Builder(String providerName, String projectName) {
            this.providerName = providerName;
            this.projectName = projectName;
        }

        public MessageReplacementValues build() {
            return new MessageReplacementValues(
                providerName,
                projectName,
                defaultIfBlank(projectVersionName),
                defaultIfBlank(componentName),
                defaultIfBlank(componentVersionName),
                defaultIfBlank(componentUsage),
                defaultIfBlank(componentLicense),
                defaultIfBlank(severity),
                defaultIfBlank(policyCategory)
            );
        }

        public Builder providerName(String providerName) {
            this.providerName = providerName;
            return this;
        }

        public Builder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public Builder projectVersionName(String projectVersionName) {
            this.projectVersionName = projectVersionName;
            return this;
        }

        public Builder componentName(String componentName) {
            this.componentName = componentName;
            return this;
        }

        public Builder componentVersionName(String componentVersionName) {
            this.componentVersionName = componentVersionName;
            return this;
        }

        public Builder componentUsage(String componentUsage) {
            this.componentUsage = componentUsage;
            return this;
        }

        public Builder componentLicense(String componentLicense) {
            this.componentLicense = componentLicense;
            return this;
        }

        public Builder severity(String severity) {
            this.severity = severity;
            return this;
        }

        public Builder policyCategory(String policyCategory) {
            this.policyCategory = policyCategory;
            return this;
        }

        private String defaultIfBlank(String replacementField) {
            if (StringUtils.isNotBlank(replacementField)) {
                return replacementField;
            } else {
                return MessageReplacementValues.DEFAULT_NOTIFICATION_REPLACEMENT_VALUE;
            }
        }
    }
}
