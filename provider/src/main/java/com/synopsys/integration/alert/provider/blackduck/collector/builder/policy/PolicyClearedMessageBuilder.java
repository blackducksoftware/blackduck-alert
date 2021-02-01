/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.blackduck.collector.builder.policy;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.CommonMessageData;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.BlackDuckMessageBuilder;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.MessageBuilderConstants;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.model.ComponentData;
import com.synopsys.integration.alert.provider.blackduck.collector.util.AlertBlackDuckService;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.api.manual.component.RuleViolationClearedNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationClearedNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.datastructure.SetMap;

@Component
public class PolicyClearedMessageBuilder extends BlackDuckMessageBuilder<RuleViolationClearedNotificationView> {
    private final Logger logger = LoggerFactory.getLogger(PolicyClearedMessageBuilder.class);
    private final PolicyCommonBuilder policyCommonBuilder;

    @Autowired
    public PolicyClearedMessageBuilder(PolicyCommonBuilder policyCommonBuilder) {
        super(NotificationType.RULE_VIOLATION_CLEARED);
        this.policyCommonBuilder = policyCommonBuilder;
    }

    @Override
    public List<ProviderMessageContent> buildMessageContents(CommonMessageData commonMessageData, RuleViolationClearedNotificationView notificationView, BlackDuckServicesFactory blackDuckServicesFactory) {
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        AlertBlackDuckService alertBlackDuckService = new AlertBlackDuckService(blackDuckApiClient);
        RuleViolationClearedNotificationContent violationContent = notificationView.getContent();

        String projectVersionUrl = violationContent.getProjectVersion();
        String projectUrl = getNullableProjectUrlFromProjectVersion(projectVersionUrl, blackDuckServicesFactory.getBlackDuckApiClient(), logger::warn);
        try {
            ProviderMessageContent.Builder messageContentBuilder = new ProviderMessageContent.Builder();
            messageContentBuilder
                .applyCommonData(commonMessageData)
                .applyTopic(MessageBuilderConstants.LABEL_PROJECT_NAME, violationContent.getProjectName(), projectUrl)
                .applySubTopic(MessageBuilderConstants.LABEL_PROJECT_VERSION_NAME, violationContent.getProjectVersionName(), projectVersionUrl);
            Map<String, PolicyInfo> policyUrlToInfoMap = DataStructureUtils.mapToValues(violationContent.getPolicyInfos(), PolicyInfo::getPolicy);
            SetMap<ComponentVersionStatus, PolicyInfo> componentPolicies = policyCommonBuilder.createComponentToPolicyMapping(violationContent.getComponentVersionStatuses(), policyUrlToInfoMap);
            DistributionJobModel job = commonMessageData.getJob();
            Collection<String> policyFilter = job.getPolicyFilterPolicyNames();
            List<ComponentItem> items = new LinkedList<>();
            for (Map.Entry<ComponentVersionStatus, Set<PolicyInfo>> componentToPolicyEntry : componentPolicies.entrySet()) {
                ComponentVersionStatus componentVersionStatus = componentToPolicyEntry.getKey();
                Set<PolicyInfo> policies = componentToPolicyEntry.getValue();
                List<ComponentItem> componentItems = retrievePolicyItems(alertBlackDuckService, componentVersionStatus, policies, commonMessageData.getNotificationId(), projectVersionUrl, policyFilter);
                items.addAll(componentItems);
            }
            messageContentBuilder.applyAllComponentItems(items);
            return List.of(messageContentBuilder.build());
        } catch (AlertException ex) {
            logger.error("Error creating policy violation cleared message.", ex);
        }

        return List.of();
    }

    private List<ComponentItem> retrievePolicyItems(AlertBlackDuckService alertBlackDuckService, ComponentVersionStatus componentVersionStatus,
        Set<PolicyInfo> policies, Long notificationId, String projectVersionUrl, Collection<String> policyFilter) {
        List<ComponentItem> componentItems = new LinkedList<>();
        String componentName = componentVersionStatus.getComponentName();
        String componentVersionName = componentVersionStatus.getComponentVersionName();
        ComponentData componentData = new ComponentData(componentName, componentVersionName, projectVersionUrl, ProjectVersionView.COMPONENTS_LINK);
        componentItems.addAll(policyCommonBuilder.retrievePolicyItems(getNotificationType(), alertBlackDuckService, componentData, policies, notificationId,
            ItemOperation.DELETE, componentVersionStatus.getBomComponent(), List.of(), policyFilter));
        return componentItems;
    }

}
