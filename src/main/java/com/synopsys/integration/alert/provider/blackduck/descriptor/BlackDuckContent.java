/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.blackduck.descriptor;

import static com.synopsys.integration.alert.common.workflow.filter.field.JsonField.createJsonPath;
import static com.synopsys.integration.alert.common.workflow.filter.field.JsonField.createLongField;
import static com.synopsys.integration.alert.common.workflow.filter.field.JsonField.createObjectField;
import static com.synopsys.integration.alert.common.workflow.filter.field.JsonField.createOptionalStringField;
import static com.synopsys.integration.alert.common.workflow.filter.field.JsonField.createStringField;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.jayway.jsonpath.TypeRef;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.FieldContentIdentifier;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.provider.ProviderContent;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonField;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.api.manual.component.VulnerabilitySourceQualifiedId;

@Component
public class BlackDuckContent extends ProviderContent {
    // common fields
    public static final String JSON_FIELD_CONTENT = "content";
    public static final String JSON_FIELD_PROJECT_NAME = "projectName";
    public static final String JSON_FIELD_PROJECT_VERSION_NAME = "projectVersionName";
    public static final String JSON_FIELD_PROJECT_VERSION = "projectVersion";
    public static final String JSON_FIELD_COMPONENT_NAME = "componentName";
    public static final String JSON_FIELD_COMPONENT = "component";
    public static final String JSON_FIELD_COMPONENT_VERSION_NAME = "componentVersionName";
    public static final String JSON_FIELD_COMPONENT_VERSION = "componentVersion";
    public static final String JSON_FIELD_BOM_COMPONENT = "bomComponent";

    // license limit fields
    public static final String JSON_FIELD_MESSAGE = "message";
    public static final String JSON_FIELD_MARKETING_URL = "marketingPageUrl";
    public static final String JSON_FIELD_CODE_SIZE = "usedCodeSize";
    public static final String JSON_FIELD_LIMIT_HARD = "hardLimit";
    public static final String JSON_FIELD_LIMIT_SOFT = "softLimit";

    // policy fields
    public static final String JSON_FIELD_COMPONENT_VERSION_STATUSES = "componentVersionStatuses[*]";
    public static final String JSON_FIELD_POLICY_INFOS = "policyInfos[*]";
    public static final String JSON_FIELD_POLICY_NAME = "policyName";
    public static final String JSON_FIELD_POLICY = "policy";
    public static final String JSON_FIELD_POLICY_SEVERITY = "severity";
    public static final String JSON_FIELD_FIRST_NAME = "firstName";
    public static final String JSON_FIELD_LAST_NAME = "lastName";

    // vulnerability fields
    public static final String JSON_FIELD_AFFECTED_PROJECT_VERSIONS = "affectedProjectVersions[*]";
    public static final String JSON_FIELD_VERSION_NAME = "versionName";
    public static final String JSON_FIELD_NEW_VULNERABILITY_IDS = "newVulnerabilityIds[*]";
    public static final String JSON_FIELD_UPDATED_VULNERABILITY_IDS = "updatedVulnerabilityIds[*]";
    public static final String JSON_FIELD_DELETED_VULNERABILITY_IDS = "deletedVulnerabilityIds[*]";

    // labels
    public static final String LABEL_COMPONENT_VERSION_STATUS = "Component Version Status";
    public static final String LABEL_COMPONENT_NAME = "Component";
    public static final String LABEL_COMPONENT_VERSION_NAME = "Component Version";
    public static final String LABEL_COMPONENT_LICENSE = "Component License";
    public static final String LABEL_POLICY_INFO_LIST = "Policy Infos";
    public static final String LABEL_POLICY_NAME = "Policy Violated";
    public static final String LABEL_POLICY_SEVERITY_NAME = "Severity";
    public static final String LABEL_PROJECT_NAME = "Project";
    public static final String LABEL_PROJECT_VERSION_NAME = "Project Version";
    public static final String LABEL_POLICY_OVERRIDE_FIRST_NAME = JSON_FIELD_FIRST_NAME;
    public static final String LABEL_POLICY_OVERRIDE_LAST_NAME = JSON_FIELD_LAST_NAME;
    public static final String LABEL_POLICY_OVERRIDE_BY = "Policy Overridden by";
    public static final String LABEL_LICENSE_LIMIT_MESSAGE = "License Limit Message";
    public static final String LABEL_LICENSE_LIMIT_USED_CODE_SIZE = "Used Code Size";
    public static final String LABEL_LICENSE_LIMIT_HARD = "Hard Limit";
    public static final String LABEL_LICENSE_LIMIT_SOFT = "Soft Limit";

    public static final String LABEL_VULNERABILITY_NEW = "New Vulnerabilities";
    public static final String LABEL_VULNERABILITY_UPDATED = "Updated Vulnerabilities";
    public static final String LABEL_VULNERABILITY_DELETED = "Deleted Vulnerabilities";
    public static final String LABEL_VULNERABILITIES = "Vulnerabilities";
    public static final String LABEL_VULNERABILITY_SEVERITY = "Severity";

    public static final String LABEL_BOM_COMPONENT = "Bom Component";

    public static final String LABEL_REMEDIATION_FIX_PREVIOUS = "Remediation - Fixes Previous Vulnerabilities";
    public static final String LABEL_REMEDIATION_CLEAN = "Remediation - Without Vulnerabilities";
    public static final String LABEL_REMEDIATION_LATEST = "Remediation - Latest Version";

    public static final ProviderContentType BOM_EDIT = new ProviderContentType(
        NotificationType.BOM_EDIT.name(),
        List.of(
            createStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_PROJECT_NAME), JSON_FIELD_PROJECT_NAME, FieldContentIdentifier.TOPIC, LABEL_PROJECT_NAME,
                List.of(ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT, ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT, ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN)),
            createStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_PROJECT_VERSION_NAME), JSON_FIELD_PROJECT_VERSION_NAME, FieldContentIdentifier.SUB_TOPIC, LABEL_PROJECT_VERSION_NAME),
            createStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_PROJECT_VERSION), JSON_FIELD_PROJECT_VERSION, FieldContentIdentifier.SUB_TOPIC_URL,
                LABEL_PROJECT_VERSION_NAME + JsonField.LABEL_URL_SUFFIX),
            createStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_BOM_COMPONENT), JSON_FIELD_BOM_COMPONENT, FieldContentIdentifier.CATEGORY_ITEM, LABEL_BOM_COMPONENT)
        ));

    public static final ProviderContentType LICENSE_LIMIT = new ProviderContentType(
        NotificationType.LICENSE_LIMIT.name(),
        List.of(
            createStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_MESSAGE), JSON_FIELD_MESSAGE, FieldContentIdentifier.TOPIC, LABEL_LICENSE_LIMIT_MESSAGE),
            createStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_MARKETING_URL), JSON_FIELD_MARKETING_URL, FieldContentIdentifier.TOPIC_URL,
                LABEL_LICENSE_LIMIT_MESSAGE + JsonField.LABEL_URL_SUFFIX),
            createLongField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_CODE_SIZE), JSON_FIELD_CODE_SIZE, FieldContentIdentifier.CATEGORY_ITEM, LABEL_LICENSE_LIMIT_USED_CODE_SIZE),
            createLongField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_LIMIT_HARD), JSON_FIELD_LIMIT_HARD, FieldContentIdentifier.CATEGORY_ITEM, LABEL_LICENSE_LIMIT_HARD),
            createLongField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_LIMIT_SOFT), JSON_FIELD_LIMIT_SOFT, FieldContentIdentifier.CATEGORY_ITEM, LABEL_LICENSE_LIMIT_SOFT)
        )
    );

    public static final ProviderContentType POLICY_OVERRIDE = new ProviderContentType(
        NotificationType.POLICY_OVERRIDE.name(),
        List.of(
            createStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_PROJECT_NAME), JSON_FIELD_PROJECT_NAME, FieldContentIdentifier.TOPIC, LABEL_PROJECT_NAME,
                List.of(ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT, ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT, ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN)),
            createStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_PROJECT_VERSION_NAME), JSON_FIELD_PROJECT_VERSION_NAME, FieldContentIdentifier.SUB_TOPIC, LABEL_PROJECT_VERSION_NAME),
            createStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_PROJECT_VERSION), JSON_FIELD_PROJECT_VERSION, FieldContentIdentifier.SUB_TOPIC_URL,
                LABEL_PROJECT_VERSION_NAME + JsonField.LABEL_URL_SUFFIX),
            createStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_BOM_COMPONENT), JSON_FIELD_BOM_COMPONENT, FieldContentIdentifier.CATEGORY_ITEM,
                LABEL_BOM_COMPONENT + JsonField.LABEL_URL_SUFFIX),
            createOptionalStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_COMPONENT_NAME), JSON_FIELD_COMPONENT_NAME, FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_NAME),
            createOptionalStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_COMPONENT), JSON_FIELD_COMPONENT, FieldContentIdentifier.CATEGORY_ITEM,
                LABEL_COMPONENT_NAME + JsonField.LABEL_URL_SUFFIX),
            createOptionalStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_COMPONENT_VERSION_NAME), JSON_FIELD_COMPONENT_VERSION_NAME, FieldContentIdentifier.CATEGORY_ITEM,
                LABEL_COMPONENT_VERSION_NAME),
            createOptionalStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_COMPONENT_VERSION), JSON_FIELD_COMPONENT_VERSION, FieldContentIdentifier.CATEGORY_ITEM,
                LABEL_COMPONENT_VERSION_NAME + JsonField.LABEL_URL_SUFFIX),
            createStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_BOM_COMPONENT), JSON_FIELD_BOM_COMPONENT, FieldContentIdentifier.CATEGORY_ITEM,
                LABEL_BOM_COMPONENT + JsonField.LABEL_URL_SUFFIX),
            createStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_FIRST_NAME), JSON_FIELD_FIRST_NAME, FieldContentIdentifier.CATEGORY_ITEM, LABEL_POLICY_OVERRIDE_FIRST_NAME),
            createStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_LAST_NAME), JSON_FIELD_LAST_NAME, FieldContentIdentifier.CATEGORY_ITEM, LABEL_POLICY_OVERRIDE_LAST_NAME),
            createObjectField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_POLICY_INFOS), JSON_FIELD_POLICY_INFOS, FieldContentIdentifier.CATEGORY_ITEM, LABEL_POLICY_INFO_LIST,
                new TypeRef<List<PolicyInfo>>() {})

        )
    );

    public static final ProviderContentType VULNERABILITY = new ProviderContentType(
        NotificationType.VULNERABILITY.name(),
        List.of(
            createStringField(createJsonPath(JsonField.FORMAT_TRIPLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_AFFECTED_PROJECT_VERSIONS, JSON_FIELD_PROJECT_NAME), JSON_FIELD_PROJECT_NAME, FieldContentIdentifier.TOPIC, LABEL_PROJECT_NAME,
                List.of(ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT, ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT, ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN)),
            createStringField(createJsonPath(JsonField.FORMAT_TRIPLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_AFFECTED_PROJECT_VERSIONS, JSON_FIELD_PROJECT_VERSION_NAME), JSON_FIELD_PROJECT_VERSION_NAME, FieldContentIdentifier.SUB_TOPIC,
                LABEL_PROJECT_VERSION_NAME),
            createStringField(createJsonPath(JsonField.FORMAT_TRIPLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_AFFECTED_PROJECT_VERSIONS, JSON_FIELD_PROJECT_VERSION), JSON_FIELD_PROJECT_VERSION, FieldContentIdentifier.SUB_TOPIC_URL,
                LABEL_PROJECT_VERSION_NAME + JsonField.LABEL_URL_SUFFIX),
            createStringField(createJsonPath(JsonField.FORMAT_TRIPLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_AFFECTED_PROJECT_VERSIONS, JSON_FIELD_BOM_COMPONENT), JSON_FIELD_BOM_COMPONENT, FieldContentIdentifier.CATEGORY_ITEM,
                LABEL_BOM_COMPONENT + JsonField.LABEL_URL_SUFFIX),
            createOptionalStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_COMPONENT_NAME), JSON_FIELD_COMPONENT_NAME, FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_NAME),
            createOptionalStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_COMPONENT), JSON_FIELD_COMPONENT, FieldContentIdentifier.CATEGORY_ITEM,
                LABEL_COMPONENT_NAME + JsonField.LABEL_URL_SUFFIX),
            createStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_VERSION_NAME), JSON_FIELD_VERSION_NAME, FieldContentIdentifier.CATEGORY_ITEM, LABEL_COMPONENT_VERSION_NAME),
            createStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_COMPONENT_VERSION), JSON_FIELD_COMPONENT_VERSION, FieldContentIdentifier.CATEGORY_ITEM,
                LABEL_COMPONENT_VERSION_NAME + JsonField.LABEL_URL_SUFFIX),
            createObjectField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_NEW_VULNERABILITY_IDS), JSON_FIELD_NEW_VULNERABILITY_IDS, FieldContentIdentifier.CATEGORY_ITEM, LABEL_VULNERABILITY_NEW,
                new TypeRef<List<VulnerabilitySourceQualifiedId>>() {}),
            createStringField(createJsonPath(JsonField.FORMAT_TRIPLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_AFFECTED_PROJECT_VERSIONS, JSON_FIELD_BOM_COMPONENT), JSON_FIELD_BOM_COMPONENT, FieldContentIdentifier.CATEGORY_ITEM,
                LABEL_BOM_COMPONENT + JsonField.LABEL_URL_SUFFIX),
            createObjectField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_UPDATED_VULNERABILITY_IDS), JSON_FIELD_UPDATED_VULNERABILITY_IDS, FieldContentIdentifier.CATEGORY_ITEM,
                LABEL_VULNERABILITY_UPDATED,
                new TypeRef<List<VulnerabilitySourceQualifiedId>>() {}),
            createObjectField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_DELETED_VULNERABILITY_IDS), JSON_FIELD_DELETED_VULNERABILITY_IDS, FieldContentIdentifier.CATEGORY_ITEM,
                LABEL_VULNERABILITY_DELETED,
                new TypeRef<List<VulnerabilitySourceQualifiedId>>() {})
        )
    );

    private static final List<JsonField<?>> RULE_VIOLATION_FIELD_LIST = List.of(
        createStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_PROJECT_NAME), JSON_FIELD_PROJECT_NAME, FieldContentIdentifier.TOPIC, LABEL_PROJECT_NAME,
            List.of(ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT, ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT, ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN)),
        createStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_PROJECT_VERSION_NAME), JSON_FIELD_PROJECT_VERSION_NAME, FieldContentIdentifier.SUB_TOPIC, LABEL_PROJECT_VERSION_NAME),
        createStringField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_PROJECT_VERSION), JSON_FIELD_PROJECT_VERSION, FieldContentIdentifier.SUB_TOPIC_URL,
            LABEL_PROJECT_VERSION_NAME + JsonField.LABEL_URL_SUFFIX),
        createObjectField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_COMPONENT_VERSION_STATUSES), JSON_FIELD_COMPONENT_VERSION_STATUSES, FieldContentIdentifier.CATEGORY_ITEM,
            LABEL_COMPONENT_VERSION_STATUS,
            new TypeRef<List<ComponentVersionStatus>>() {}),
        createStringField(createJsonPath(JsonField.FORMAT_TRIPLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_COMPONENT_VERSION_STATUSES, JSON_FIELD_BOM_COMPONENT), JSON_FIELD_BOM_COMPONENT, FieldContentIdentifier.CATEGORY_ITEM,
            LABEL_BOM_COMPONENT + JsonField.LABEL_URL_SUFFIX),
        createObjectField(createJsonPath(JsonField.FORMAT_DOUBLE_REPLACEMENT, JSON_FIELD_CONTENT, JSON_FIELD_POLICY_INFOS), JSON_FIELD_POLICY_INFOS, FieldContentIdentifier.CATEGORY_ITEM, LABEL_POLICY_INFO_LIST,
            new TypeRef<List<PolicyInfo>>() {})
    );
    public static final ProviderContentType RULE_VIOLATION = new ProviderContentType(
        NotificationType.RULE_VIOLATION.name(),
        RULE_VIOLATION_FIELD_LIST
    );
    public static final ProviderContentType RULE_VIOLATION_CLEARED = new ProviderContentType(
        NotificationType.RULE_VIOLATION_CLEARED.name(),
        RULE_VIOLATION_FIELD_LIST
    );

    private static final Set<ProviderContentType> SUPPORTED_CONTENT_TYPES = Set.of(LICENSE_LIMIT, POLICY_OVERRIDE, RULE_VIOLATION, RULE_VIOLATION_CLEARED, VULNERABILITY, BOM_EDIT);
    private static final EnumSet<FormatType> SUPPORTED_CONTENT_FORMATS = EnumSet.of(FormatType.DEFAULT, FormatType.DIGEST, FormatType.SUMMARY);

    public BlackDuckContent() {
        super(BlackDuckProvider.COMPONENT_NAME, SUPPORTED_CONTENT_TYPES, SUPPORTED_CONTENT_FORMATS);
    }

    @Override
    public Set<ProviderContentType> getContentTypes() {
        return super.getContentTypes();
    }

}
