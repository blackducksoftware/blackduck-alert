/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
