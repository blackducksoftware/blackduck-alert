/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.provider.blackduck;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.alert.common.enumeration.FieldContentIdentifier;
import com.synopsys.integration.alert.common.field.HierarchicalField;
import com.synopsys.integration.alert.common.field.ObjectHierarchicalField;
import com.synopsys.integration.alert.common.field.StringHierarchicalField;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.notification.content.VulnerabilitySourceQualifiedId;

public class BlackDuckProviderContentTypes {
    public static final String LABEL_COMPONENT_NAME = "componentName";
    public static final String LABEL_COMPONENT_VERSION_NAME = "componentVersionName";
    public static final String LABEL_POLICY_NAME = "policyName";
    public static final String LABEL_PROJECT_NAME = "projectName";
    public static final String LABEL_PROJECT_VERSION_NAME = "projectVersionName";

    // vulnerability
    // TODO remove suffix from the constant name
    public static final String LABEL_SUFFIX_VULNERABILITY_NEW = "newVulnerabilities";
    public static final String LABEL_SUFFIX_VULNERABILITY_UPDATED = "updatedVulnerabilities";
    public static final String LABEL_SUFFIX_VULNERABILITY_DELETED = "deletedVulnerabilities";
    public static final List<ProviderContentType> ALL = new ArrayList();
    public static final ProviderContentType BOM_EDIT = new ProviderContentType(
        NotificationType.BOM_EDIT.name(),
        Collections.emptyList()
    );
    public static final ProviderContentType LICENSE_LIMIT = new ProviderContentType(
        NotificationType.LICENSE_LIMIT.name(),
        Collections.emptyList()
    );
    public static final ProviderContentType POLICY_OVERRIDE = new ProviderContentType(
        NotificationType.POLICY_OVERRIDE.name(),
        Arrays.asList(
            new StringHierarchicalField(Arrays.asList("content"), "projectName", FieldContentIdentifier.TOPIC, LABEL_PROJECT_NAME, "configuredProjects"),
            new StringHierarchicalField(Arrays.asList("content"), "projectVersionName", FieldContentIdentifier.SUB_TOPIC, LABEL_PROJECT_VERSION_NAME),
            new StringHierarchicalField(Arrays.asList("content"), "projectVersion", FieldContentIdentifier.SUB_TOPIC_URL, LABEL_PROJECT_VERSION_NAME + HierarchicalField.LABEL_URL_SUFFIX),
            new StringHierarchicalField(Arrays.asList("content"), "componentName", FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_NAME),
            new StringHierarchicalField(Arrays.asList("content"), "component", FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_NAME + HierarchicalField.LABEL_URL_SUFFIX),
            new StringHierarchicalField(Arrays.asList("content"), "versionName", FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_VERSION_NAME),
            new StringHierarchicalField(Arrays.asList("content"), "componentVersion", FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_VERSION_NAME + HierarchicalField.LABEL_URL_SUFFIX),
            new StringHierarchicalField(Arrays.asList("content", "policyInfos"), "policyName", FieldContentIdentifier.CATEGORY_ITEM, LABEL_POLICY_NAME),
            new StringHierarchicalField(Arrays.asList("content", "policyInfos"), "policy", FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_VERSION_NAME + HierarchicalField.LABEL_URL_SUFFIX)
        )
    );
    private static final Type VULNERABILITY_TYPE = new TypeToken<VulnerabilitySourceQualifiedId>() {}.getType();
    public static final ProviderContentType VULNERABILITY = new ProviderContentType(
        NotificationType.VULNERABILITY.name(),
        Arrays.asList(
            new StringHierarchicalField(Arrays.asList("content", "affectedProjectVersions"), "projectName", FieldContentIdentifier.TOPIC, LABEL_PROJECT_NAME, "configuredProjects"),
            new StringHierarchicalField(Arrays.asList("content", "affectedProjectVersions"), "projectVersionName", FieldContentIdentifier.SUB_TOPIC, LABEL_PROJECT_VERSION_NAME),
            new StringHierarchicalField(Arrays.asList("content", "affectedProjectVersions"), "projectVersion", FieldContentIdentifier.SUB_TOPIC_URL, LABEL_PROJECT_VERSION_NAME + HierarchicalField.LABEL_URL_SUFFIX),
            new StringHierarchicalField(Arrays.asList("content"), "componentName", FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_NAME),
            new StringHierarchicalField(Arrays.asList("content"), "component", FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_NAME + HierarchicalField.LABEL_URL_SUFFIX),
            new StringHierarchicalField(Arrays.asList("content"), "versionName", FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_VERSION_NAME),
            new StringHierarchicalField(Arrays.asList("content"), "componentVersion", FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_VERSION_NAME + HierarchicalField.LABEL_URL_SUFFIX),
            new ObjectHierarchicalField(Arrays.asList("content"), "newVulnerabilityIds", FieldContentIdentifier.CATEGORY_ITEM, LABEL_SUFFIX_VULNERABILITY_NEW, VULNERABILITY_TYPE),
            new ObjectHierarchicalField(Arrays.asList("content"), "updatedVulnerabilityIds", FieldContentIdentifier.CATEGORY_ITEM, LABEL_SUFFIX_VULNERABILITY_UPDATED, VULNERABILITY_TYPE),
            new ObjectHierarchicalField(Arrays.asList("content"), "deletedVulnerabilityIds", FieldContentIdentifier.CATEGORY_ITEM, LABEL_SUFFIX_VULNERABILITY_DELETED, VULNERABILITY_TYPE)
        )
    );
    private static final List<HierarchicalField> RULE_VIOLATION_FIELD_LIST = Arrays.asList(
        new StringHierarchicalField(Arrays.asList("content"), "projectName", FieldContentIdentifier.TOPIC, LABEL_PROJECT_NAME, "configuredProjects"),
        new StringHierarchicalField(Arrays.asList("content"), "projectVersionName", FieldContentIdentifier.SUB_TOPIC, LABEL_PROJECT_VERSION_NAME),
        new StringHierarchicalField(Arrays.asList("content"), "projectVersion", FieldContentIdentifier.SUB_TOPIC_URL, LABEL_PROJECT_VERSION_NAME + HierarchicalField.LABEL_URL_SUFFIX),
        new StringHierarchicalField(Arrays.asList("content", "componentVersionStatuses"), "componentName", FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_NAME),
        new StringHierarchicalField(Arrays.asList("content", "componentVersionStatuses"), "component", FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_NAME + HierarchicalField.LABEL_URL_SUFFIX),
        new StringHierarchicalField(Arrays.asList("content", "componentVersionStatuses"), "componentVersionName", FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_VERSION_NAME),
        new StringHierarchicalField(Arrays.asList("content", "componentVersionStatuses"), "componentVersion", FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_VERSION_NAME + HierarchicalField.LABEL_URL_SUFFIX),
        new StringHierarchicalField(Arrays.asList("content", "policyInfos"), "policyName", FieldContentIdentifier.CATEGORY_ITEM, LABEL_POLICY_NAME),
        new StringHierarchicalField(Arrays.asList("content", "policyInfos"), "policy", FieldContentIdentifier.CATEGORY_ITEM, LABEL_POLICY_NAME + HierarchicalField.LABEL_URL_SUFFIX)
    );
    public static final ProviderContentType RULE_VIOLATION = new ProviderContentType(
        NotificationType.RULE_VIOLATION.name(),
        RULE_VIOLATION_FIELD_LIST
    );
    public static final ProviderContentType RULE_VIOLATION_CLEARED = new ProviderContentType(
        NotificationType.RULE_VIOLATION_CLEARED.name(),
        RULE_VIOLATION_FIELD_LIST
    );

    static {
        ALL.add(BOM_EDIT);
        ALL.add(LICENSE_LIMIT);
        ALL.add(POLICY_OVERRIDE);
        ALL.add(RULE_VIOLATION);
        ALL.add(RULE_VIOLATION_CLEARED);
        ALL.add(VULNERABILITY);
    }

    private BlackDuckProviderContentTypes() {
        // This class should not be instantiated
    }
}
