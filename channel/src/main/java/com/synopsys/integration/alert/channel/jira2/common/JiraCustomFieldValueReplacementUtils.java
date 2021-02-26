/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira2.common;

import java.util.Optional;
import java.util.function.Supplier;

import com.synopsys.integration.alert.channel.jira2.common.model.JiraCustomFieldConfig;
import com.synopsys.integration.alert.channel.jira2.common.model.JiraCustomFieldReplacementValues;

public final class JiraCustomFieldValueReplacementUtils {
    public static final String REPLACEMENT_PROVIDER_NAME = "{{providerName}}";
    public static final String REPLACEMENT_PROJECT_NAME = "{{projectName}}";
    public static final String REPLACEMENT_PROJECT_VERSION = "{{projectVersion}}";
    public static final String REPLACEMENT_COMPONENT_NAME = "{{componentName}}";
    public static final String REPLACEMENT_COMPONENT_VERSION = "{{componentVersion}}";

    private static final Supplier<Optional<String>> DEFAULT_REPLACEMENT_SUPPLIER = () -> Optional.of(JiraCustomFieldReplacementValues.DEFAULT_REPLACEMENT);

    public static void injectReplacementFieldValue(JiraCustomFieldConfig jiraCustomField, JiraCustomFieldReplacementValues replacementValues) {
        String originalValue = jiraCustomField.getFieldOriginalValue();
        extractReplacementValue(originalValue, replacementValues)
            .ifPresent(jiraCustomField::setFieldReplacementValue);
    }

    private static Optional<String> extractReplacementValue(String originalFieldValue, JiraCustomFieldReplacementValues replacementValues) {
        switch (originalFieldValue) {
            case REPLACEMENT_PROVIDER_NAME:
                return Optional.of(replacementValues.getProviderName());
            case REPLACEMENT_PROJECT_NAME:
                return Optional.of(replacementValues.getProjectName());
            case REPLACEMENT_PROJECT_VERSION:
                return replacementValues.getProjectVersionName().or(DEFAULT_REPLACEMENT_SUPPLIER);
            case REPLACEMENT_COMPONENT_NAME:
                return replacementValues.getComponentName().or(DEFAULT_REPLACEMENT_SUPPLIER);
            case REPLACEMENT_COMPONENT_VERSION:
                return replacementValues.getComponentVersionName().or(DEFAULT_REPLACEMENT_SUPPLIER);
            default:
                return Optional.empty();
        }
    }

    private JiraCustomFieldValueReplacementUtils() {
        // This class should not be instantiated
    }

}
