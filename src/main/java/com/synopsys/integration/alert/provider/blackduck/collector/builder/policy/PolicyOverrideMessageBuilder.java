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
package com.synopsys.integration.alert.provider.blackduck.collector.builder.policy;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.CommonMessageData;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.BlackDuckMessageBuilder;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.MessageBuilderConstants;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.model.ComponentData;
import com.synopsys.integration.alert.provider.blackduck.collector.util.BlackDuckResponseCache;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.api.manual.component.PolicyOverrideNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.PolicyOverrideNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;

@Component
public class PolicyOverrideMessageBuilder implements BlackDuckMessageBuilder<PolicyOverrideNotificationView> {
    private final Logger logger = LoggerFactory.getLogger(PolicyOverrideMessageBuilder.class);
    private PolicyCommonBuilder policyCommonBuilder;

    @Autowired
    public PolicyOverrideMessageBuilder(PolicyCommonBuilder policyCommonBuilder) {
        this.policyCommonBuilder = policyCommonBuilder;
    }

    @Override
    public String getNotificationType() {
        return NotificationType.POLICY_OVERRIDE.name();
    }

    @Override
    public List<ProviderMessageContent> buildMessageContents(CommonMessageData commonMessageData, PolicyOverrideNotificationView notificationView, BlackDuckBucket blackDuckBucket, BlackDuckServicesFactory blackDuckServicesFactory) {
        long timeout = blackDuckServicesFactory.getBlackDuckHttpClient().getTimeoutInSeconds();
        BlackDuckBucketService bucketService = blackDuckServicesFactory.createBlackDuckBucketService();
        BlackDuckResponseCache responseCache = new BlackDuckResponseCache(bucketService, blackDuckBucket, timeout);
        PolicyOverrideNotificationContent overrideContent = notificationView.getContent();

        String projectName = overrideContent.getProjectName();
        String projectUrl = retrieveNullableProjectUrlAndLog(projectName, blackDuckServicesFactory.createProjectService(), logger::warn);
        try {
            ProviderMessageContent.Builder messageContentBuilder = new ProviderMessageContent.Builder();
            messageContentBuilder
                .applyCommonData(commonMessageData)
                .applyTopic(MessageBuilderConstants.LABEL_PROJECT_NAME, overrideContent.getProjectName(), projectUrl)
                .applySubTopic(MessageBuilderConstants.LABEL_PROJECT_VERSION_NAME, overrideContent.getProjectVersionName(), overrideContent.getProjectVersion());

            List<PolicyInfo> policies = overrideContent.getPolicyInfos();
            FieldAccessor fieldAccessor = commonMessageData.getJob().getFieldAccessor();
            Collection<String> policyFilter = fieldAccessor.getAllStrings(BlackDuckDescriptor.KEY_BLACKDUCK_POLICY_NOTIFICATION_TYPE_FILTER);
            List<ComponentItem> items = retrievePolicyItems(responseCache, overrideContent, policies, commonMessageData.getNotificationId(), overrideContent.getProjectVersion(), policyFilter);
            messageContentBuilder.applyAllComponentItems(items);
            return List.of(messageContentBuilder.build());
        } catch (AlertException ex) {
            logger.error("Error creating policy override message.", ex);
        }

        return List.of();
    }

    private List<ComponentItem> retrievePolicyItems(BlackDuckResponseCache blackDuckResponseCache, PolicyOverrideNotificationContent overrideContent,
        Collection<PolicyInfo> policies, Long notificationId, String projectVersionUrl, Collection<String> policyFilter) {
        String firstName = overrideContent.getFirstName();
        String lastName = overrideContent.getLastName();

        String overrideBy = String.format("%s %s", firstName, lastName);
        LinkableItem policyOverride = new LinkableItem(MessageBuilderConstants.LABEL_POLICY_OVERRIDE_BY, overrideBy);

        String componentName = overrideContent.getComponentName();
        String componentVersionName = overrideContent.getComponentVersionName();
        ComponentData componentData = new ComponentData(componentName, componentVersionName, projectVersionUrl, ProjectVersionView.COMPONENTS_LINK);
        return policyCommonBuilder.retrievePolicyItems(blackDuckResponseCache, componentData, policies, notificationId, ItemOperation.DELETE, null, List.of(policyOverride), policyFilter);
    }

}
