/*
 * api-channel-jira
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution.custom;

import org.apache.commons.lang3.StringUtils;

public final class JiraCustomFieldValueReplacementResolver {
    public static final String REPLACEMENT_PROVIDER_NAME = "{{providerName}}";
    public static final String REPLACEMENT_PROJECT_NAME = "{{projectName}}";
    public static final String REPLACEMENT_PROJECT_VERSION = "{{projectVersion}}";
    public static final String REPLACEMENT_COMPONENT_NAME = "{{componentName}}";
    public static final String REPLACEMENT_COMPONENT_VERSION = "{{componentVersion}}";

    private JiraCustomFieldReplacementValues replacementValues;

    public JiraCustomFieldValueReplacementResolver(JiraCustomFieldReplacementValues replacementValues) {
        this.replacementValues = replacementValues;
    }

    public void injectReplacementFieldValue(JiraCustomFieldConfig jiraCustomField) {
        String originalValue = jiraCustomField.getFieldOriginalValue();
        jiraCustomField.setFieldReplacementValue(replaceFieldValues(originalValue, replacementValues));
    }

    private static String replaceFieldValues(String originalFieldValue, JiraCustomFieldReplacementValues replacementValues) {
        String replacedFieldValue = StringUtils.replace(originalFieldValue, REPLACEMENT_PROVIDER_NAME, replacementValues.getProviderName());
        replacedFieldValue = StringUtils.replace(replacedFieldValue, REPLACEMENT_PROJECT_NAME, replacementValues.getProjectName());
        replacedFieldValue = StringUtils.replace(replacedFieldValue, REPLACEMENT_PROJECT_VERSION, replacementValues.getProjectVersionName().orElse(JiraCustomFieldReplacementValues.DEFAULT_REPLACEMENT));
        replacedFieldValue = StringUtils.replace(replacedFieldValue, REPLACEMENT_COMPONENT_NAME, replacementValues.getComponentName().orElse(JiraCustomFieldReplacementValues.DEFAULT_REPLACEMENT));
        replacedFieldValue = StringUtils.replace(replacedFieldValue, REPLACEMENT_COMPONENT_VERSION, replacementValues.getComponentVersionName().orElse(JiraCustomFieldReplacementValues.DEFAULT_REPLACEMENT));
        return replacedFieldValue;
    }

}
