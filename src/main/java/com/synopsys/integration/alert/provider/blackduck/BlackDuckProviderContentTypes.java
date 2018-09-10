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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.synopsys.integration.alert.common.field.HierarchicalField;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class BlackDuckProviderContentTypes {
    public static final String LABEL_SUFFIX_COMPONENT_NAME = "componentName";
    public static final String LABEL_SUFFIX_COMPONENT_URL = "componentUrl";
    public static final String LABEL_SUFFIX_COMPONENT_VERSION_NAME = "componentVersionName";
    public static final String LABEL_SUFFIX_COMPONENT_VERSION_URL = "componentVersionUrl";
    public static final String LABEL_SUFFIX_POLICY_NAME = "policyName";
    public static final String LABEL_SUFFIX_POLICY_URL = "policyUrl";

    // vulnerability
    public static final String LABEL_SUFFIX_VULNERABILITY_NEW_SOURCE = "newSource";
    public static final String LABEL_SUFFIX_VULNERABILITY_NEW_ID = "newVulnerabilityId";
    public static final String LABEL_SUFFIX_VULNERABILITY_NEW_URL = "newVulnerabilityUrl";

    public static final String LABEL_SUFFIX_VULNERABILITY_UPDATED_SOURCE = "updatedSource";
    public static final String LABEL_SUFFIX_VULNERABILITY_UPDATED_ID = "updatedVulnerabilityId";
    public static final String LABEL_SUFFIX_VULNERABILITY_UPDATED_URL = "updatedVulnerabilityUrl";

    public static final String LABEL_SUFFIX_VULNERABILITY_DELETED_SOURCE = "deletedSource";
    public static final String LABEL_SUFFIX_VULNERABILITY_DELETED_ID = "newVulnerabilityId";
    public static final String LABEL_SUFFIX_VULNERABILITY_DELETED_URL = "newVulnerabilityUrl";

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
            new HierarchicalField(Arrays.asList("content"), "projectName", HierarchicalField.LABEL_TOPIC, "configuredProjects"),
            new HierarchicalField(Arrays.asList("content"), "projectVersionName", HierarchicalField.LABEL_SUB_TOPIC),
            new HierarchicalField(Arrays.asList("content"), "projectVersion", HierarchicalField.LABEL_SUB_TOPIC + HierarchicalField.LABEL_URL_SUFFIX),
            new HierarchicalField(Arrays.asList("content"), "componentName", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_COMPONENT_NAME),
            new HierarchicalField(Arrays.asList("content"), "component", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_COMPONENT_URL),
            new HierarchicalField(Arrays.asList("content"), "versionName", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_COMPONENT_VERSION_NAME),
            new HierarchicalField(Arrays.asList("content"), "componentVersion", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_COMPONENT_VERSION_URL),
            new HierarchicalField(Arrays.asList("content", "policyInfos"), "policyName", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_POLICY_NAME),
            new HierarchicalField(Arrays.asList("content", "policyInfos"), "policy", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_POLICY_URL)
        )
    );
    public static final ProviderContentType VULNERABILITY = new ProviderContentType(
        NotificationType.VULNERABILITY.name(),
        Arrays.asList(
            new HierarchicalField(Arrays.asList("content", "affectedProjectVersions"), "projectName", HierarchicalField.LABEL_TOPIC, "configuredProjects"),
            new HierarchicalField(Arrays.asList("content", "affectedProjectVersions"), "projectVersionName", HierarchicalField.LABEL_SUB_TOPIC),
            new HierarchicalField(Arrays.asList("content", "affectedProjectVersions"), "projectVersion", HierarchicalField.LABEL_SUB_TOPIC + HierarchicalField.LABEL_URL_SUFFIX),
            new HierarchicalField(Arrays.asList("content"), "componentName", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_COMPONENT_NAME),
            new HierarchicalField(Arrays.asList("content"), "component", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_COMPONENT_URL),
            new HierarchicalField(Arrays.asList("content"), "versionName", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_COMPONENT_VERSION_NAME),
            new HierarchicalField(Arrays.asList("content"), "componentVersion", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_COMPONENT_VERSION_URL),
            new HierarchicalField(Arrays.asList("content", "newVulnerabilityIds"), "source", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_VULNERABILITY_NEW_SOURCE),
            new HierarchicalField(Arrays.asList("content", "newVulnerabilityIds"), "vulnerabilityId", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_VULNERABILITY_NEW_ID),
            new HierarchicalField(Arrays.asList("content", "newVulnerabilityIds"), "vulnerability", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_VULNERABILITY_NEW_URL),
            new HierarchicalField(Arrays.asList("content", "updatedVulnerabilityIds"), "source", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_VULNERABILITY_UPDATED_SOURCE),
            new HierarchicalField(Arrays.asList("content", "updatedVulnerabilityIds"), "vulnerabilityId", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_VULNERABILITY_UPDATED_ID),
            new HierarchicalField(Arrays.asList("content", "updatedVulnerabilityIds"), "vulnerability", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_VULNERABILITY_UPDATED_URL),
            new HierarchicalField(Arrays.asList("content", "deletedVulnerabilityIds"), "source", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_VULNERABILITY_DELETED_SOURCE),
            new HierarchicalField(Arrays.asList("content", "deletedVulnerabilityIds"), "vulnerabilityId", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_VULNERABILITY_DELETED_ID),
            new HierarchicalField(Arrays.asList("content", "deletedVulnerabilityIds"), "vulnerability", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_VULNERABILITY_DELETED_URL)

        )
    );
    private static final List<HierarchicalField> RULE_VIOLATION_FIELD_LIST = Arrays.asList(
        new HierarchicalField(Arrays.asList("content"), "projectName", HierarchicalField.LABEL_TOPIC, "configuredProjects"),
        new HierarchicalField(Arrays.asList("content"), "projectVersionName", HierarchicalField.LABEL_SUB_TOPIC),
        new HierarchicalField(Arrays.asList("content"), "projectVersion", HierarchicalField.LABEL_SUB_TOPIC + HierarchicalField.LABEL_URL_SUFFIX),
        new HierarchicalField(Arrays.asList("content", "componentVersionStatuses"), "componentName", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_COMPONENT_NAME),
        new HierarchicalField(Arrays.asList("content", "componentVersionStatuses"), "component", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_COMPONENT_URL),
        new HierarchicalField(Arrays.asList("content", "componentVersionStatuses"), "componentVersionName", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_COMPONENT_VERSION_NAME),
        new HierarchicalField(Arrays.asList("content", "componentVersionStatuses"), "componentVersion", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_COMPONENT_VERSION_URL),
        new HierarchicalField(Arrays.asList("content", "policyInfos"), "policyName", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_POLICY_NAME),
        new HierarchicalField(Arrays.asList("content", "policyInfos"), "policy", HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX + LABEL_SUFFIX_POLICY_URL)
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
