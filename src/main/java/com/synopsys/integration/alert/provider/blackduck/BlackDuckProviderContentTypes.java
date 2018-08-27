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

import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.workflow.filter.HierarchicalField;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class BlackDuckProviderContentTypes { 
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
            HierarchicalField.nestedField("content", "projectName"),
            HierarchicalField.nestedField("content", "projectVersionName"),
            HierarchicalField.nestedField("content", "componentName"),
            HierarchicalField.nestedField("content", "versionName")
        )
    );
    public static final ProviderContentType RULE_VIOLATION = new ProviderContentType(
        NotificationType.RULE_VIOLATION.name(),
        Arrays.asList(
            HierarchicalField.nestedField("content", "projectName"),
            HierarchicalField.nestedField("content", "projectVersionName"),
            HierarchicalField.nestedField("content", "componentVersionStatuses", "componentName"),
            HierarchicalField.nestedField("content", "componentVersionStatuses", "componentVersionName")
        )
    );
    public static final ProviderContentType RULE_VIOLATION_CLEARED = new ProviderContentType(
        NotificationType.RULE_VIOLATION_CLEARED.name(),
        Arrays.asList(
            HierarchicalField.nestedField("content", "projectName"),
            HierarchicalField.nestedField("content", "projectVersionName"),
            HierarchicalField.nestedField("content", "componentVersionStatuses", "componentName"),
            HierarchicalField.nestedField("content", "componentVersionStatuses", "componentVersionName")
        )
    );
    public static final ProviderContentType VULNERABILITY = new ProviderContentType(
        NotificationType.VULNERABILITY.name(),
        Arrays.asList(
            HierarchicalField.nestedField("content", "affectedProjectVersions", "projectName"),
            HierarchicalField.nestedField("content", "affectedProjectVersions", "projectVersionName"),
            HierarchicalField.nestedField("content", "componentName"),
            HierarchicalField.nestedField("content", "versionName")
        )
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
