/*
 * test-common-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.test.common.blackduck;

import java.math.BigDecimal;
import java.util.List;

import com.synopsys.integration.alert.provider.blackduck.processor.model.RuleViolationUniquePolicyNotificationContent;
import com.synopsys.integration.blackduck.api.core.ResourceLink;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceLongTermView;
import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceLongTermVulnerabilityRiskView;
import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceShortTermView;
import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceShortTermVulnerabilityRiskView;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionComponentVersionLicensesView;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.synopsys.integration.blackduck.api.generated.enumeration.UsageType;
import com.synopsys.integration.blackduck.api.generated.response.ComponentVersionUpgradeGuidanceView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.api.manual.component.RuleViolationNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.temporary.component.VersionBomOriginView;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationNotificationView;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class BlackDuckResponseTestUtility {
    public ProjectVersionComponentVersionView createProjectVersionComponentVersionView() throws IntegrationException {
        ProjectVersionComponentVersionView projectVersionComponentVersionView = new ProjectVersionComponentVersionView();

        projectVersionComponentVersionView.setComponentName("component name");
        projectVersionComponentVersionView.setComponentVersion("http://component-version-url");
        projectVersionComponentVersionView.setComponentVersionName("component version name");
        projectVersionComponentVersionView.setPolicyStatus(ProjectVersionComponentPolicyStatusType.IN_VIOLATION);
        projectVersionComponentVersionView.setUsages(List.of(UsageType.DYNAMICALLY_LINKED));

        ProjectVersionComponentVersionLicensesView projectVersionComponentVersionLicensesView = new ProjectVersionComponentVersionLicensesView();
        projectVersionComponentVersionLicensesView.setLicense("http://license-link");
        projectVersionComponentVersionLicensesView.setLicenseDisplay("license-display");
        projectVersionComponentVersionView.setLicenses(List.of(projectVersionComponentVersionLicensesView));

        ResourceLink resourceLink = new ResourceLink();
        resourceLink.setHref(new HttpUrl("https://resource-url"));
        resourceLink.setRel("policy-rules");
        ResourceMetadata meta = new ResourceMetadata();
        meta.setHref(new HttpUrl("https://meta-url"));
        meta.setLinks(List.of(resourceLink));
        projectVersionComponentVersionView.setMeta(meta);

        VersionBomOriginView versionBomOriginView = new VersionBomOriginView();
        projectVersionComponentVersionView.setOrigins(List.of(versionBomOriginView));

        return projectVersionComponentVersionView;
    }

    public RuleViolationNotificationView createRuleViolationNotificationView(String projectName, String projectVersionName) {
        RuleViolationNotificationContent notificationContent = createRuleViolationNotificationContent(projectName, projectVersionName);

        RuleViolationNotificationView notificationView = new RuleViolationNotificationView();
        notificationView.setContent(notificationContent);
        notificationView.setType(NotificationType.RULE_VIOLATION);

        return notificationView;
    }

    public RuleViolationUniquePolicyNotificationContent createRuleViolationUniquePolicyNotificationContent(String projectName, String projectVersionName) {
        int componentVersionsInViolation = 1;
        RuleViolationNotificationContent notificationContent = createRuleViolationNotificationContent(projectName, projectVersionName);

        return new RuleViolationUniquePolicyNotificationContent(
            projectName,
            notificationContent.getProjectVersionName(),
            notificationContent.getProjectVersion(),
            componentVersionsInViolation,
            notificationContent.getComponentVersionStatuses(),
            notificationContent.getPolicyInfos().get(0)
        );
    }

    public static ComponentVersionUpgradeGuidanceView createComponentVersionUpgradeGuidanceView(String projectVersionName) {
        ComponentVersionUpgradeGuidanceView componentVersionUpgradeGuidanceView = new ComponentVersionUpgradeGuidanceView();
        componentVersionUpgradeGuidanceView.setShortTerm(createShortTermUpgradeGuidance(projectVersionName));
        componentVersionUpgradeGuidanceView.setLongTerm(createLongTermUpgradeGuidance(projectVersionName));
        return componentVersionUpgradeGuidanceView;
    }

    public static ComponentVersionUpgradeGuidanceShortTermView createShortTermUpgradeGuidance(String projectVersionName) {
        ComponentVersionUpgradeGuidanceShortTermView shortTermView = new ComponentVersionUpgradeGuidanceShortTermView();
        ComponentVersionUpgradeGuidanceShortTermVulnerabilityRiskView shortTermRiskView = new ComponentVersionUpgradeGuidanceShortTermVulnerabilityRiskView();
        shortTermRiskView.setCritical(BigDecimal.valueOf(0L));
        shortTermRiskView.setHigh(BigDecimal.valueOf(0L));
        shortTermRiskView.setMedium(BigDecimal.valueOf(0L));
        shortTermRiskView.setLow(BigDecimal.valueOf(0L));
        shortTermView.setVulnerabilityRisk(shortTermRiskView);
        shortTermView.setVersionName(projectVersionName);
        return shortTermView;
    }

    public static ComponentVersionUpgradeGuidanceLongTermView createLongTermUpgradeGuidance(String projectVersionName) {
        ComponentVersionUpgradeGuidanceLongTermView longTermView = new ComponentVersionUpgradeGuidanceLongTermView();
        ComponentVersionUpgradeGuidanceLongTermVulnerabilityRiskView longTermRiskView = new ComponentVersionUpgradeGuidanceLongTermVulnerabilityRiskView();
        longTermRiskView.setCritical(BigDecimal.valueOf(0L));
        longTermRiskView.setHigh(BigDecimal.valueOf(0L));
        longTermRiskView.setMedium(BigDecimal.valueOf(0L));
        longTermRiskView.setLow(BigDecimal.valueOf(0L));
        longTermView.setVulnerabilityRisk(longTermRiskView);
        longTermView.setVersionName(projectVersionName);
        return longTermView;
    }

    private RuleViolationNotificationContent createRuleViolationNotificationContent(String projectName, String projectVersionName) {
        RuleViolationNotificationContent notificationContent = new RuleViolationNotificationContent();
        notificationContent.setProjectName(projectName);
        notificationContent.setProjectVersionName(projectVersionName);
        notificationContent.setProjectVersion("https://a-project-version");
        notificationContent.setComponentVersionsInViolation(1);

        PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.setPolicy("https://a-policy");
        policyInfo.setPolicyName("a policy");
        policyInfo.setSeverity(PolicyRuleSeverityType.MAJOR.name());
        notificationContent.setPolicyInfos(List.of(policyInfo));

        ComponentVersionStatus componentVersionStatus = new ComponentVersionStatus();
        componentVersionStatus.setBomComponent("https://bom-component");
        componentVersionStatus.setComponentName("component name");
        componentVersionStatus.setComponent("https://component");
        componentVersionStatus.setComponentVersionName("component-version name");
        componentVersionStatus.setComponentVersion("https://component-version");
        componentVersionStatus.setPolicies(List.of(policyInfo.getPolicy()));
        componentVersionStatus.setBomComponentVersionPolicyStatus(ProjectVersionComponentPolicyStatusType.IN_VIOLATION.name());
        componentVersionStatus.setComponentIssueLink("https://component-issues");
        notificationContent.setComponentVersionStatuses(List.of(componentVersionStatus));

        return notificationContent;
    }
}
