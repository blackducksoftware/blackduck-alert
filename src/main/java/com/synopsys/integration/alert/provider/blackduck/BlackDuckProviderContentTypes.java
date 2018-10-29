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
    //common fields
    public static final String JSON_FIELD_CONTENT = "content";
    public static final String JSON_FIELD_PROJECT_NAME = "projectName";
    public static final String JSON_FIELD_PROJECT_VERSION_NAME = "projectVersionName";
    public static final String JSON_FIELD_PROJECT_VERSION = "projectVersion";
    public static final String JSON_FIELD_COMPONENT_NAME = "componentName";
    public static final String JSON_FIELD_COMPONENT = "component";
    public static final String JSON_FIELD_COMPONENT_VERSION_NAME = "componentVersionName";
    public static final String JSON_FIELD_COMPONENT_VERSION = "componentVersion";

    public static final String CONFIG_MAPPING_CONFIGURED_PROJECTS = "configuredProjects";

    // policy fields
    public static final String JSON_FIELD_COMPONENT_VERSION_STATUSES = "componentVersionStatuses";
    public static final String JSON_FIELD_POLICY_INFOS = "policyInfos";
    public static final String JSON_FIELD_POLICY_NAME = "policyName";
    public static final String JSON_FIELD_POLICY = "policy";
    public static final String JSON_FIELD_FIRST_NAME = "firstName";
    public static final String JSON_FIELD_LAST_NAME = "lastName";

    // vulnerability fields
    public static final String JSON_FIELD_AFFECTED_PROJECT_VERSIONS = "affectedProjectVersions";
    public static final String JSON_FIELD_VERSION_NAME = "versionName";
    public static final String JSON_FIELD_NEW_VULNERABILITY_IDS = "newVulnerabilityIds";
    public static final String JSON_FIELD_UPDATED_VULNERABILITY_IDS = "updatedVulnerabilityIds";
    public static final String JSON_FIELD_DELETED_VULNERABILITY_IDS = "deletedVulnerabilityIds";

    // labels
    public static final String LABEL_COMPONENT_NAME = "Component";
    public static final String LABEL_COMPONENT_VERSION_NAME = "Component Version";
    public static final String LABEL_POLICY_NAME = "Policy";
    public static final String LABEL_PROJECT_NAME = "Project";
    public static final String LABEL_PROJECT_VERSION_NAME = "Project Version";
    public static final String LABEL_POLICY_OVERRIDE_FIRST_NAME = JSON_FIELD_FIRST_NAME;
    public static final String LABEL_POLICY_OVERRIDE_LAST_NAME = JSON_FIELD_LAST_NAME;
    public static final String LABEL_POLICY_OVERRIDE_BY = "Policy Overridden by";

    public static final String LABEL_VULNERABILITY_NEW = "New Vulnerabilities";
    public static final String LABEL_VULNERABILITY_UPDATED = "Updated Vulnerabilities";
    public static final String LABEL_VULNERABILITY_DELETED = "Deleted Vulnerabilities";
    public static final String LABEL_VULNERABILITY_SEVERITY = "Severity";

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
            new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT), JSON_FIELD_PROJECT_NAME, FieldContentIdentifier.TOPIC, LABEL_PROJECT_NAME, CONFIG_MAPPING_CONFIGURED_PROJECTS),
            new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT), JSON_FIELD_PROJECT_VERSION_NAME, FieldContentIdentifier.SUB_TOPIC, LABEL_PROJECT_VERSION_NAME),
            new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT), JSON_FIELD_PROJECT_VERSION, FieldContentIdentifier.SUB_TOPIC_URL, LABEL_PROJECT_VERSION_NAME + HierarchicalField.LABEL_URL_SUFFIX),
            new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT), JSON_FIELD_COMPONENT_NAME, FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_NAME),
            new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT), JSON_FIELD_COMPONENT, FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_NAME + HierarchicalField.LABEL_URL_SUFFIX),
            new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT), JSON_FIELD_COMPONENT_VERSION_NAME, FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_VERSION_NAME),
            new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT), JSON_FIELD_COMPONENT_VERSION, FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_VERSION_NAME + HierarchicalField.LABEL_URL_SUFFIX),
            new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT), JSON_FIELD_FIRST_NAME, FieldContentIdentifier.CATEGORY_ITEM, LABEL_POLICY_OVERRIDE_FIRST_NAME),
            new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT), JSON_FIELD_LAST_NAME, FieldContentIdentifier.CATEGORY_ITEM, LABEL_POLICY_OVERRIDE_LAST_NAME),
            new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT, JSON_FIELD_POLICY_INFOS), JSON_FIELD_POLICY_NAME, FieldContentIdentifier.CATEGORY_ITEM, LABEL_POLICY_NAME),
            new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT, JSON_FIELD_POLICY_INFOS), JSON_FIELD_POLICY, FieldContentIdentifier.CATEGORY_ITEM, LABEL_POLICY_NAME + HierarchicalField.LABEL_URL_SUFFIX)
        )
    );
    private static final Type VULNERABILITY_TYPE = new TypeToken<VulnerabilitySourceQualifiedId>() {}.getType();

    public static final ProviderContentType VULNERABILITY = new ProviderContentType(
        NotificationType.VULNERABILITY.name(),
        Arrays.asList(
            new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT, JSON_FIELD_AFFECTED_PROJECT_VERSIONS), JSON_FIELD_PROJECT_NAME, FieldContentIdentifier.TOPIC, LABEL_PROJECT_NAME, CONFIG_MAPPING_CONFIGURED_PROJECTS),
            new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT, JSON_FIELD_AFFECTED_PROJECT_VERSIONS), JSON_FIELD_PROJECT_VERSION_NAME, FieldContentIdentifier.SUB_TOPIC, LABEL_PROJECT_VERSION_NAME),
            new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT, JSON_FIELD_AFFECTED_PROJECT_VERSIONS), JSON_FIELD_PROJECT_VERSION, FieldContentIdentifier.SUB_TOPIC_URL,
                LABEL_PROJECT_VERSION_NAME + HierarchicalField.LABEL_URL_SUFFIX),
            new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT), JSON_FIELD_COMPONENT_NAME, FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_NAME),
            new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT), JSON_FIELD_COMPONENT, FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_NAME + HierarchicalField.LABEL_URL_SUFFIX),
            new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT), JSON_FIELD_VERSION_NAME, FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_VERSION_NAME),
            new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT), JSON_FIELD_COMPONENT_VERSION, FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_VERSION_NAME + HierarchicalField.LABEL_URL_SUFFIX),
            new ObjectHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT), JSON_FIELD_NEW_VULNERABILITY_IDS, FieldContentIdentifier.CATEGORY_ITEM, LABEL_VULNERABILITY_NEW, VULNERABILITY_TYPE),
            new ObjectHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT), JSON_FIELD_UPDATED_VULNERABILITY_IDS, FieldContentIdentifier.CATEGORY_ITEM, LABEL_VULNERABILITY_UPDATED, VULNERABILITY_TYPE),
            new ObjectHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT), JSON_FIELD_DELETED_VULNERABILITY_IDS, FieldContentIdentifier.CATEGORY_ITEM, LABEL_VULNERABILITY_DELETED, VULNERABILITY_TYPE)
        )
    );

    private static final List<HierarchicalField> RULE_VIOLATION_FIELD_LIST = Arrays.asList(
        new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT), JSON_FIELD_PROJECT_NAME, FieldContentIdentifier.TOPIC, LABEL_PROJECT_NAME, CONFIG_MAPPING_CONFIGURED_PROJECTS),
        new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT), JSON_FIELD_PROJECT_VERSION_NAME, FieldContentIdentifier.SUB_TOPIC, LABEL_PROJECT_VERSION_NAME),
        new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT), JSON_FIELD_PROJECT_VERSION, FieldContentIdentifier.SUB_TOPIC_URL, LABEL_PROJECT_VERSION_NAME + HierarchicalField.LABEL_URL_SUFFIX),
        new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT, JSON_FIELD_COMPONENT_VERSION_STATUSES), JSON_FIELD_COMPONENT_NAME, FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_NAME),
        new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT, JSON_FIELD_COMPONENT_VERSION_STATUSES), JSON_FIELD_COMPONENT, FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_NAME + HierarchicalField.LABEL_URL_SUFFIX),
        new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT, JSON_FIELD_COMPONENT_VERSION_STATUSES), JSON_FIELD_COMPONENT_VERSION_NAME, FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_VERSION_NAME),
        new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT, JSON_FIELD_COMPONENT_VERSION_STATUSES), JSON_FIELD_COMPONENT_VERSION, FieldContentIdentifier.CATEGORY_ITEM,
            LABEL_COMPONENT_VERSION_NAME + HierarchicalField.LABEL_URL_SUFFIX),
        new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT, JSON_FIELD_POLICY_INFOS), JSON_FIELD_POLICY_NAME, FieldContentIdentifier.CATEGORY_ITEM, LABEL_POLICY_NAME),
        new StringHierarchicalField(Arrays.asList(JSON_FIELD_CONTENT, JSON_FIELD_POLICY_INFOS), JSON_FIELD_POLICY, FieldContentIdentifier.CATEGORY_ITEM, LABEL_POLICY_NAME + HierarchicalField.LABEL_URL_SUFFIX)
    );
    public static final ProviderContentType RULE_VIOLATION = new ProviderContentType(
        NotificationType.RULE_VIOLATION.name(),
        RULE_VIOLATION_FIELD_LIST
    );
    public static final ProviderContentType RULE_VIOLATION_CLEARED = new ProviderContentType(
        NotificationType.RULE_VIOLATION_CLEARED.name(),
        RULE_VIOLATION_FIELD_LIST
    );

    public static final List<ProviderContentType> ALL = Arrays.asList(BOM_EDIT, LICENSE_LIMIT, POLICY_OVERRIDE, RULE_VIOLATION, RULE_VIOLATION_CLEARED, VULNERABILITY);

    private BlackDuckProviderContentTypes() {
        // This class should not be instantiated
    }
}
