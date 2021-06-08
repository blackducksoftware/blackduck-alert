/*
 * api-channel-jira
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

public final class JiraCustomFieldValueReplacementUtils {
    public static final String REPLACEMENT_PROVIDER_NAME = "{{providerName}}";
    public static final String REPLACEMENT_PROJECT_NAME = "{{projectName}}";
    public static final String REPLACEMENT_PROJECT_VERSION = "{{projectVersion}}";
    public static final String REPLACEMENT_COMPONENT_NAME = "{{componentName}}";
    public static final String REPLACEMENT_COMPONENT_VERSION = "{{componentVersion}}";

    private static final Supplier<Optional<String>> DEFAULT_REPLACEMENT_SUPPLIER = () -> Optional.of(JiraCustomFieldReplacementValues.DEFAULT_REPLACEMENT);

    public static void injectReplacementFieldValue(JiraCustomFieldConfig jiraCustomField, JiraCustomFieldReplacementValues replacementValues) {
        String originalValue = jiraCustomField.getFieldOriginalValue();
        replaceFieldValues(originalValue, replacementValues);
        //parseReplacementString(originalValue, replacementValues)
        //    .ifPresent(jiraCustomField::setFieldReplacementValue);
        //extractReplacementValue(originalValue, replacementValues)
        //    .ifPresent(jiraCustomField::setFieldReplacementValue);
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

    private static Optional<String> parseReplacementString(String originalFieldValue, JiraCustomFieldReplacementValues replacementValues) {
        List<String> validReplacementStrings = new ArrayList<>();

        for (String replacementString : getReplacementStrings()) {
            if (originalFieldValue.contains(replacementString)) {
                validReplacementStrings.add(replacementString);
            }
        }

        if (validReplacementStrings.isEmpty()) {
            return Optional.empty();
        }

        String replacedFieldValue = originalFieldValue;
        for (String validReplacementString : validReplacementStrings) {
            replacedFieldValue = setReplacementValues(replacedFieldValue, validReplacementString, replacementValues);
        }

        return Optional.of(replacedFieldValue);
    }

    private static String setReplacementValues(String replacedFieldValue, String validReplacementString, JiraCustomFieldReplacementValues replacementValues) {
        switch (validReplacementString) {
            case REPLACEMENT_PROVIDER_NAME:
                return StringUtils.replace(replacedFieldValue, validReplacementString, replacementValues.getProviderName());
            case REPLACEMENT_PROJECT_NAME:
                return StringUtils.replace(replacedFieldValue, validReplacementString, replacementValues.getProjectName());
            case REPLACEMENT_PROJECT_VERSION:
                //TODO: In the old implementation we are returning a supplier to return an optional  of JiraCustomFieldReplacementValues.DEFAULT_REPLACEMENT which is equal to "None"
                //  This is true for the 2 cases below as well.
                return StringUtils.replace(replacedFieldValue, validReplacementString, replacementValues.getProjectVersionName().orElse(JiraCustomFieldReplacementValues.DEFAULT_REPLACEMENT));
            case REPLACEMENT_COMPONENT_NAME:
                return StringUtils.replace(replacedFieldValue, validReplacementString, replacementValues.getComponentName().orElse(JiraCustomFieldReplacementValues.DEFAULT_REPLACEMENT));
            case REPLACEMENT_COMPONENT_VERSION:
                return StringUtils.replace(replacedFieldValue, validReplacementString, replacementValues.getComponentVersionName().orElse(JiraCustomFieldReplacementValues.DEFAULT_REPLACEMENT));
            default:
                return ""; //TODO: need a better default case since we can't use Optional.empty(). Maybe throw an exception?
        }
    }

    private static List<String> getReplacementStrings() {
        return List.of(REPLACEMENT_PROVIDER_NAME, REPLACEMENT_PROJECT_NAME, REPLACEMENT_PROJECT_VERSION, REPLACEMENT_COMPONENT_NAME, REPLACEMENT_COMPONENT_VERSION); //TODO see if theres a better way to do it than this
        //EnumSet myset = EnumSet.allOf(JiraCustomFieldValueReplacementUtils.class);
    }

    private static String replaceFieldValues(String originalFieldValue, JiraCustomFieldReplacementValues replacementValues) {
        String replacedFieldValue = StringUtils.replace(originalFieldValue, REPLACEMENT_PROVIDER_NAME, replacementValues.getProviderName());
        replacedFieldValue = StringUtils.replace(replacedFieldValue, REPLACEMENT_PROJECT_NAME, replacementValues.getProjectName());
        replacedFieldValue = StringUtils.replace(replacedFieldValue, REPLACEMENT_PROJECT_VERSION, replacementValues.getProjectVersionName().orElse(JiraCustomFieldReplacementValues.DEFAULT_REPLACEMENT));
        replacedFieldValue = StringUtils.replace(replacedFieldValue, REPLACEMENT_COMPONENT_NAME, replacementValues.getComponentName().orElse(JiraCustomFieldReplacementValues.DEFAULT_REPLACEMENT));
        replacedFieldValue = StringUtils.replace(replacedFieldValue, REPLACEMENT_COMPONENT_VERSION, replacementValues.getComponentVersionName().orElse(JiraCustomFieldReplacementValues.DEFAULT_REPLACEMENT));
        return replacedFieldValue;
    }

    private JiraCustomFieldValueReplacementUtils() {
        // This class should not be instantiated
    }

}
