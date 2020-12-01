/**
 * provider
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
package com.synopsys.integration.alert.provider.blackduck.collector.builder.util;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.MessageBuilderConstants;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.model.ComponentData;
import com.synopsys.integration.alert.provider.blackduck.collector.util.BlackDuckResponseCache;
import com.synopsys.integration.blackduck.api.generated.enumeration.UsageType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;

public final class ComponentBuilderUtil {
    private ComponentBuilderUtil() {
    }

    public static List<LinkableItem> getLicenseLinkableItems(ProjectVersionComponentView bomComponentView) {
        return bomComponentView.getLicenses()
                   .stream()
                   .map(licenseView -> new LinkableItem(MessageBuilderConstants.LABEL_COMPONENT_LICENSE, licenseView.getLicenseDisplay()))
                   .collect(Collectors.toList());
    }

    public static List<LinkableItem> getUsageLinkableItems(ProjectVersionComponentView bomComponentView) {
        return bomComponentView.getUsages()
                   .stream()
                   .map(UsageType::prettyPrint)
                   .map(usage -> new LinkableItem(MessageBuilderConstants.LABEL_COMPONENT_USAGE, usage))
                   .collect(Collectors.toList());
    }

    public static LinkableItem createPolicyNameItem(PolicyInfo policyInfo) {
        String policyName = policyInfo.getPolicyName();
        return new LinkableItem(MessageBuilderConstants.LABEL_POLICY_NAME, policyName);
    }

    public static Optional<LinkableItem> createPolicySeverityItem(PolicyInfo policyInfo) {
        String severity = policyInfo.getSeverity();
        if (StringUtils.isNotBlank(severity)) {
            LinkableItem severityItem = new LinkableItem(MessageBuilderConstants.LABEL_POLICY_SEVERITY_NAME, severity);
            return Optional.of(severityItem);
        }
        return Optional.empty();
    }

    public static void applyComponentInformation(ComponentItem.Builder componentBuilder, BlackDuckResponseCache responseCache, ComponentData componentData) {
        String projectQueryLink = responseCache.getProjectComponentQueryLink(componentData.getProjectVersionUrl(), componentData.getProjectComponentLink(), componentData.getComponentName()).orElse(null);
        componentData.getComponentVersionName()
            .filter(StringUtils::isNotBlank)
            .ifPresentOrElse(componentVersion -> {
                    componentBuilder.applyComponentData(new LinkableItem(MessageBuilderConstants.LABEL_COMPONENT_NAME, componentData.getComponentName()));
                    componentBuilder.applySubComponent(new LinkableItem(MessageBuilderConstants.LABEL_COMPONENT_VERSION_NAME, componentVersion, projectQueryLink));
                },
                () -> componentBuilder.applyComponentData(new LinkableItem(MessageBuilderConstants.LABEL_COMPONENT_NAME, componentData.getComponentName(), projectQueryLink)));
    }

}
