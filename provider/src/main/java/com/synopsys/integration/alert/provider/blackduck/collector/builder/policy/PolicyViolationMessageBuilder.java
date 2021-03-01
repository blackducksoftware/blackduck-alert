/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.collector.builder.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ComponentItemPriority;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.CommonMessageData;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ComponentItemCallbackInfo;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.BlackDuckIssueTrackerCallbackUtility;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.BlackDuckMessageBuilder;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.MessageBuilderConstants;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.model.ComponentData;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.ComponentBuilderUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.util.VulnerabilityUtil;
import com.synopsys.integration.alert.provider.blackduck.collector.util.AlertBlackDuckService;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionExpressionsView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionVulnerableBomComponentsView;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.api.manual.component.RuleViolationNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ComponentService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.datastructure.SetMap;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

@Component
public class PolicyViolationMessageBuilder extends BlackDuckMessageBuilder<RuleViolationNotificationView> {
    private final Logger logger = LoggerFactory.getLogger(PolicyViolationMessageBuilder.class);
    private final PolicyCommonBuilder policyCommonBuilder;
    private final BlackDuckIssueTrackerCallbackUtility blackDuckIssueTrackerCallbackUtility;

    @Autowired
    public PolicyViolationMessageBuilder(PolicyCommonBuilder policyCommonBuilder, BlackDuckIssueTrackerCallbackUtility blackDuckIssueTrackerCallbackUtility) {
        super(NotificationType.RULE_VIOLATION);
        this.policyCommonBuilder = policyCommonBuilder;
        this.blackDuckIssueTrackerCallbackUtility = blackDuckIssueTrackerCallbackUtility;
    }

    @Override
    public List<ProviderMessageContent> buildMessageContents(CommonMessageData commonMessageData, RuleViolationNotificationView notificationView, BlackDuckServicesFactory blackDuckServicesFactory) {
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        AlertBlackDuckService alertBlackDuckService = new AlertBlackDuckService(blackDuckApiClient);
        ComponentService componentService = blackDuckServicesFactory.createComponentService();
        RuleViolationNotificationContent violationContent = notificationView.getContent();

        String projectVersionUrl = violationContent.getProjectVersion();
        String projectUrl = getNullableProjectUrlFromProjectVersion(projectVersionUrl, blackDuckApiClient, logger::warn);
        try {
            ProviderMessageContent.Builder messageContentBuilder = new ProviderMessageContent.Builder();
            messageContentBuilder
                .applyCommonData(commonMessageData)
                .applyTopic(MessageBuilderConstants.LABEL_PROJECT_NAME, violationContent.getProjectName(), projectUrl)
                .applySubTopic(MessageBuilderConstants.LABEL_PROJECT_VERSION_NAME, violationContent.getProjectVersionName(), projectVersionUrl);
            Map<String, PolicyInfo> policyUrlToInfoMap = DataStructureUtils.mapToValues(violationContent.getPolicyInfos(), PolicyInfo::getPolicy);
            SetMap<ComponentVersionStatus, PolicyInfo> componentPolicies = policyCommonBuilder.createComponentToPolicyMapping(violationContent.getComponentVersionStatuses(), policyUrlToInfoMap);
            DistributionJobModel job = commonMessageData.getJob();
            Collection<String> policyFilters = job.getPolicyFilterPolicyNames();
            List<ComponentItem> items = new LinkedList<>();
            for (Map.Entry<ComponentVersionStatus, Set<PolicyInfo>> componentToPolicyEntry : componentPolicies.entrySet()) {
                ComponentVersionStatus componentVersionStatus = componentToPolicyEntry.getKey();
                Set<PolicyInfo> policies = componentToPolicyEntry.getValue();
                List<ComponentItem> componentItems = retrievePolicyItems(alertBlackDuckService, blackDuckApiClient, componentService, componentVersionStatus, policies, commonMessageData.getNotificationId(),
                    violationContent.getProjectVersion(), policyFilters);
                items.addAll(componentItems);
            }
            messageContentBuilder.applyAllComponentItems(items);
            return List.of(messageContentBuilder.build());
        } catch (AlertException ex) {
            logger.error("Error creating policy violation message.", ex);
        }

        return List.of();
    }

    private List<ComponentItem> retrievePolicyItems(AlertBlackDuckService alertBlackDuckService, BlackDuckApiClient blackDuckApiClient, ComponentService componentService, ComponentVersionStatus componentVersionStatus,
        Set<PolicyInfo> policies, Long notificationId, String projectVersionUrl, Collection<String> policyFilters) {
        List<ComponentItem> componentItems = new LinkedList<>();
        String componentName = componentVersionStatus.getComponentName();
        String componentVersionName = componentVersionStatus.getComponentVersionName();
        String bomComponentUrl = componentVersionStatus.getBomComponent();

        Optional<ProjectVersionComponentView> optionalBomComponent = alertBlackDuckService.getBomComponentView(bomComponentUrl);
        ComponentData componentData = new ComponentData(componentName, componentVersionName, projectVersionUrl, ProjectVersionView.COMPONENTS_LINK);
        componentItems.addAll(
            policyCommonBuilder.retrievePolicyItems(getNotificationType(), alertBlackDuckService, componentData, policies, notificationId, ItemOperation.ADD, bomComponentUrl, List.of(), policyFilters));
        for (PolicyInfo policyInfo : policies) {
            String policyName = policyInfo.getPolicyName();
            if (policyFilters.isEmpty() || policyFilters.contains(policyName)) {
                LinkableItem policyNameItem = ComponentBuilderUtil.createPolicyNameItem(policyInfo);
                LinkableItem nullablePolicySeverityItem = ComponentBuilderUtil.createPolicySeverityItem(policyInfo).orElse(null);
                Optional<PolicyRuleView> optionalPolicyRule = alertBlackDuckService.getPolicyRule(policyInfo);
                List<PolicyRuleExpressionExpressionsView> expressions = optionalPolicyRule.map(rule -> rule.getExpression().getExpressions()).orElse(List.of());
                if (optionalBomComponent.isPresent() && policyCommonBuilder.hasVulnerabilityRule(expressions)) {
                    List<ComponentItem> vulnerabilityPolicyItems = createVulnerabilityPolicyItems(
                        alertBlackDuckService, blackDuckApiClient, componentService, optionalBomComponent.get(), policyNameItem, nullablePolicySeverityItem, projectVersionUrl, componentName, componentVersionName, notificationId);
                    componentItems.addAll(vulnerabilityPolicyItems);
                }
            }
        }
        return componentItems;
    }

    private List<ComponentItem> createVulnerabilityPolicyItems(AlertBlackDuckService alertBlackDuckService, BlackDuckApiClient blackDuckApiClient, ComponentService componentService, ProjectVersionComponentView bomComponent,
        LinkableItem policyNameItem, LinkableItem policySeverity, String projectVersionUrl, String componentName, String componentVersionName, Long notificationId) {
        List<ComponentItem> vulnerabilityPolicyItems = new ArrayList<>();

        Optional<ProjectVersionWrapper> optionalProjectVersionWrapper = alertBlackDuckService.getProjectVersionWrapper(projectVersionUrl);
        if (optionalProjectVersionWrapper.isPresent()) {
            try {
                ProjectVersionWrapper projectVersionWrapper = optionalProjectVersionWrapper.get();
                ComponentData componentData = new ComponentData(componentName, componentVersionName, projectVersionUrl, ProjectVersionView.COMPONENTS_LINK);
                Optional<ComponentItemCallbackInfo> callbackInfo = blackDuckIssueTrackerCallbackUtility.createCallbackInfo(getNotificationType(), bomComponent);

                List<ProjectVersionVulnerableBomComponentsView> vulnerableComponentViews = VulnerabilityUtil.getVulnerableComponentViews(blackDuckApiClient, projectVersionWrapper, bomComponent);
                List<ComponentItem> vulnerabilityComponentItems =
                    policyCommonBuilder.createVulnerabilityPolicyComponentItems(vulnerableComponentViews, policyNameItem, policySeverity, componentData, callbackInfo.orElse(null), notificationId, blackDuckApiClient, alertBlackDuckService);
                vulnerabilityPolicyItems.addAll(vulnerabilityComponentItems);
                ComponentVersionView componentVersionView = blackDuckApiClient.getResponse(new HttpUrl(bomComponent.getComponentVersion()), ComponentVersionView.class);

                Optional<ComponentItem> remediationComponentItem = createRemediationComponentItem(alertBlackDuckService, MessageBuilderConstants.CATEGORY_TYPE_POLICY, componentService, componentVersionView, componentData, policyNameItem,
                    policySeverity, true, notificationId);
                remediationComponentItem.ifPresent(vulnerabilityPolicyItems::add);
            } catch (IntegrationException e) {
                logger.debug(String.format("Could not get the project/version. Skipping vulnerability info for this policy: %s. Exception: %s", policyNameItem, e.getMessage()), e);
            }
        }
        return vulnerabilityPolicyItems;
    }

    protected Optional<ComponentItem> createRemediationComponentItem(AlertBlackDuckService alertBlackDuckService, String categoryType, ComponentService componentService, ComponentVersionView componentVersionView,
        ComponentData componentData, LinkableItem categoryItem, LinkableItem severityItem, boolean collapseOnCategory, Long notificationId) {
        try {
            List<LinkableItem> remediationItems = VulnerabilityUtil.getRemediationItems(componentService, componentVersionView);
            if (!remediationItems.isEmpty()) {
                ComponentItem.Builder remediationComponent = new ComponentItem.Builder()
                                                                 .applyCategory(categoryType)
                                                                 .applyOperation(ItemOperation.INFO)
                                                                 .applyPriority(ComponentItemPriority.NONE)
                                                                 .applyCategoryItem(categoryItem)
                                                                 .applyCategoryGroupingAttribute(severityItem)
                                                                 .applyCollapseOnCategory(collapseOnCategory)
                                                                 .applyAllComponentAttributes(remediationItems)
                                                                 .applyNotificationId(notificationId);
                ComponentBuilderUtil.applyComponentInformation(remediationComponent, alertBlackDuckService, componentData);
                // TODO should this get callbackInfo?
                return Optional.of(remediationComponent.build());
            }
        } catch (IntegrationException e) {
            logger.debug("Could not create remediation component", e);
        }
        return Optional.empty();
    }

}
