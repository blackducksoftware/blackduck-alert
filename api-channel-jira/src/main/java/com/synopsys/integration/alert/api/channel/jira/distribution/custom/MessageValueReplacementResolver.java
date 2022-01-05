/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution.custom;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

public final class MessageValueReplacementResolver {
    public static final String REPLACEMENT_PROVIDER_NAME = "{{providerName}}";
    public static final String REPLACEMENT_PROVIDER_TYPE = "{{providerType}}";
    public static final String REPLACEMENT_PROJECT_NAME = "{{projectName}}";
    public static final String REPLACEMENT_PROJECT_VERSION = "{{projectVersion}}";
    public static final String REPLACEMENT_COMPONENT_NAME = "{{componentName}}";
    public static final String REPLACEMENT_COMPONENT_VERSION = "{{componentVersion}}";
    public static final String REPLACEMENT_COMPONENT_USAGE = "{{componentUsage}}";
    public static final String REPLACEMENT_COMPONENT_LICENSE = "{{componentLicense}}";
    public static final String REPLACEMENT_SEVERITY = "{{severity}}";
    public static final String REPLACEMENT_POLICY_CATEGORY = "{{policyCategory}}";
    public static final String REPLACEMENT_SHORT_TERM_UPGRADE_GUIDANCE = "{{shortTermUpgradeGuidance}}";
    public static final String REPLACEMENT_LONG_TERM_UPGRADE_GUIDANCE = "{{longTermUpgradeGuidance}}";

    public static final int JIRA_CUSTOM_FIELD_LENGTH = 255;

    private final MessageReplacementValues replacementValues;

    public MessageValueReplacementResolver(MessageReplacementValues replacementValues) {
        this.replacementValues = replacementValues;
    }

    public String createReplacedFieldValue(String originalValue) {
        // provider name is the old name used for provider type which will be phased out in v7
        String modifiedFieldValue = StringUtils.replace(originalValue, REPLACEMENT_PROVIDER_NAME, replacementValues.getProviderType());
        modifiedFieldValue = StringUtils.replace(modifiedFieldValue, REPLACEMENT_PROVIDER_TYPE, replacementValues.getProviderType());
        modifiedFieldValue = StringUtils.replace(modifiedFieldValue, REPLACEMENT_PROJECT_NAME, replacementValues.getProjectName());

        modifiedFieldValue = replaceFieldValue(modifiedFieldValue, REPLACEMENT_PROJECT_VERSION, replacementValues::getProjectVersionName);
        modifiedFieldValue = replaceFieldValue(modifiedFieldValue, REPLACEMENT_COMPONENT_NAME, replacementValues::getComponentName);
        modifiedFieldValue = replaceFieldValue(modifiedFieldValue, REPLACEMENT_COMPONENT_VERSION, replacementValues::getComponentVersionName);
        modifiedFieldValue = replaceFieldValue(modifiedFieldValue, REPLACEMENT_COMPONENT_USAGE, replacementValues::getComponentUsage);
        modifiedFieldValue = replaceFieldValue(modifiedFieldValue, REPLACEMENT_COMPONENT_LICENSE, replacementValues::getComponentLicense);
        modifiedFieldValue = replaceFieldValue(modifiedFieldValue, REPLACEMENT_SEVERITY, replacementValues::getSeverity);
        modifiedFieldValue = replaceFieldValue(modifiedFieldValue, REPLACEMENT_POLICY_CATEGORY, replacementValues::getPolicyCategory);
        modifiedFieldValue = replaceFieldValue(modifiedFieldValue, REPLACEMENT_SHORT_TERM_UPGRADE_GUIDANCE, replacementValues::getShortTermUpgradeGuidance);
        modifiedFieldValue = replaceFieldValue(modifiedFieldValue, REPLACEMENT_LONG_TERM_UPGRADE_GUIDANCE, replacementValues::getLongTermUpgradeGuidance);

        return StringUtils.truncate(modifiedFieldValue, JIRA_CUSTOM_FIELD_LENGTH);
    }

    private String replaceFieldValue(String originalValue, String replacementString, Supplier<Optional<String>> foundReplacementValue) {
        Optional<String> foundReplacement = foundReplacementValue.get();
        if (foundReplacement.isPresent()) {
            return StringUtils.replace(originalValue, replacementString, foundReplacement.get());
        }
        return originalValue;
    }

}
