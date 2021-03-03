/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
import com.synopsys.integration.alert.provider.blackduck.collector.util.AlertBlackDuckService;
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

    public static void applyComponentInformation(ComponentItem.Builder componentBuilder, AlertBlackDuckService alertBlackDuckService, ComponentData componentData) {
        String projectQueryLink = alertBlackDuckService.getProjectComponentQueryLink(componentData.getProjectVersionUrl(), componentData.getProjectComponentLink(), componentData.getComponentName()).orElse(null);
        componentData.getComponentVersionName()
            .filter(StringUtils::isNotBlank)
            .ifPresentOrElse(componentVersion -> {
                    componentBuilder.applyComponentData(new LinkableItem(MessageBuilderConstants.LABEL_COMPONENT_NAME, componentData.getComponentName()));
                    componentBuilder.applySubComponent(new LinkableItem(MessageBuilderConstants.LABEL_COMPONENT_VERSION_NAME, componentVersion, projectQueryLink));
                },
                () -> componentBuilder.applyComponentData(new LinkableItem(MessageBuilderConstants.LABEL_COMPONENT_NAME, componentData.getComponentName(), projectQueryLink)));
    }

}
