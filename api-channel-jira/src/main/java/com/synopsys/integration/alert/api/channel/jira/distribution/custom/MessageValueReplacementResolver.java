/*
 * api-channel-jira
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution.custom;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

public final class MessageValueReplacementResolver {
    public static final String REPLACEMENT_PROVIDER_NAME = "{{providerName}}";
    public static final String REPLACEMENT_PROJECT_NAME = "{{projectName}}";
    public static final String REPLACEMENT_PROJECT_VERSION = "{{projectVersion}}";
    public static final String REPLACEMENT_COMPONENT_NAME = "{{componentName}}";
    public static final String REPLACEMENT_COMPONENT_VERSION = "{{componentVersion}}";
    public static final String REPLACEMENT_SEVERITY = "{{severity}}";

    private final MessageReplacementValues replacementValues;

    public MessageValueReplacementResolver(MessageReplacementValues replacementValues) {
        this.replacementValues = replacementValues;
    }

    public String createReplacedFieldValue(String originalValue) {
        String modifiedFieldValue = StringUtils.replace(originalValue, REPLACEMENT_PROVIDER_NAME, replacementValues.getProviderName());
        modifiedFieldValue = StringUtils.replace(modifiedFieldValue, REPLACEMENT_PROJECT_NAME, replacementValues.getProjectName());

        modifiedFieldValue = replaceFieldValue(modifiedFieldValue, REPLACEMENT_PROJECT_VERSION, replacementValues::getProjectVersionName);
        modifiedFieldValue = replaceFieldValue(modifiedFieldValue, REPLACEMENT_COMPONENT_NAME, replacementValues::getComponentName);
        modifiedFieldValue = replaceFieldValue(modifiedFieldValue, REPLACEMENT_COMPONENT_VERSION, replacementValues::getComponentVersionName);
        modifiedFieldValue = replaceFieldValue(modifiedFieldValue, REPLACEMENT_SEVERITY, replacementValues::getSeverity);

        return modifiedFieldValue;
    }

    private String replaceFieldValue(String originalValue, String replacementString, Supplier<Optional<String>> foundReplacementValue) {
        Optional<String> foundReplacement = foundReplacementValue.get();
        if (foundReplacement.isPresent()) {
            return StringUtils.replace(originalValue, replacementString, foundReplacement.get());
        }
        return originalValue;
    }

}
