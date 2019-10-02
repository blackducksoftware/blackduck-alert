/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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

import java.util.EnumSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.provider.ProviderContent;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

@Component
// FIXME should this be removed?
public class BlackDuckContent extends ProviderContent {
    // common fields
    public static final String JSON_FIELD_CONTENT = "content";
    public static final String JSON_FIELD_PROJECT_NAME = "projectName";
    public static final String JSON_FIELD_PROJECT = "project";
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

    // project fields
    public static final String JSON_FIELD_OPERATION_TYPE = "operationType";

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
    public static final String LABEL_COMPONENT_USAGE = "Component Usage";
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
    public static final String LABEL_OPERATION_TYPE = "Operation Type";

    public static final String LABEL_REMEDIATION_FIX_PREVIOUS = "Remediation - Fixes Previous Vulnerabilities";
    public static final String LABEL_REMEDIATION_CLEAN = "Remediation - Without Vulnerabilities";
    public static final String LABEL_REMEDIATION_LATEST = "Remediation - Latest Version";

    public static final ProviderContentType BOM_EDIT = new ProviderContentType(
        NotificationType.BOM_EDIT.name()
    );

    public static final ProviderContentType LICENSE_LIMIT = new ProviderContentType(
        NotificationType.LICENSE_LIMIT.name()
    );

    public static final ProviderContentType POLICY_OVERRIDE = new ProviderContentType(
        NotificationType.POLICY_OVERRIDE.name()
    );

    public static final ProviderContentType PROJECT = new ProviderContentType(
        NotificationType.PROJECT.name()
    );

    public static final ProviderContentType PROJECT_VERSION = new ProviderContentType(
        NotificationType.PROJECT_VERSION.name()
    );

    public static final ProviderContentType VULNERABILITY = new ProviderContentType(
        NotificationType.VULNERABILITY.name()
    );

    public static final ProviderContentType RULE_VIOLATION = new ProviderContentType(
        NotificationType.RULE_VIOLATION.name()
    );

    public static final ProviderContentType RULE_VIOLATION_CLEARED = new ProviderContentType(
        NotificationType.RULE_VIOLATION_CLEARED.name()
    );

    private static final Set<ProviderContentType> SUPPORTED_CONTENT_TYPES = Set.of(LICENSE_LIMIT, POLICY_OVERRIDE, RULE_VIOLATION, RULE_VIOLATION_CLEARED, VULNERABILITY, BOM_EDIT, PROJECT, PROJECT_VERSION);
    private static final EnumSet<FormatType> SUPPORTED_CONTENT_FORMATS = EnumSet.of(FormatType.DEFAULT, FormatType.DIGEST, FormatType.SUMMARY);

    @Autowired
    public BlackDuckContent(BlackDuckProviderKey blackDuckProviderKey) {
        super(blackDuckProviderKey, SUPPORTED_CONTENT_TYPES, SUPPORTED_CONTENT_FORMATS);
    }

    @Override
    public Set<ProviderContentType> getContentTypes() {
        return super.getContentTypes();
    }

}
