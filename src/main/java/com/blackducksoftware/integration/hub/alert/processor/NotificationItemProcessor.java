/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.blackducksoftware.integration.hub.alert.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.notification.NotificationResults;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;

public class NotificationItemProcessor {
    private final List<NotificationTypeProcessor<?>> processorList;

    public NotificationItemProcessor(final List<NotificationTypeProcessor<?>> processorList) {
        this.processorList = processorList;
    }

    // public void init(final GlobalProperties globalProperties, final IntLogger intLogger) {
    // HubServicesFactory hubServicesFactory;
    // try {
    // hubServicesFactory = globalProperties.createHubServicesFactory(intLogger);
    //
    // final ProjectService projectService = hubServicesFactory.createProjectService();
    // final MapProcessorCache policyCache = new UserNotificationCache(projectService);
    // final VulnerabilityCache vulnerabilityCache = new VulnerabilityCache(projectService, hubServicesFactory);
    // // getCacheList().add(policyCache);
    // // getCacheList().add(vulnerabilityCache);
    // // getProcessorMap().put(PolicyViolationContentItem.class, new PolicyViolationProcessor(policyCache, intLogger));
    // // getProcessorMap().put(PolicyViolationClearedContentItem.class, new PolicyViolationClearedProcessor(policyCache, intLogger));
    // // getProcessorMap().put(PolicyOverrideContentItem.class, new PolicyOverrideProcessor(policyCache, intLogger));
    // // getProcessorMap().put(VulnerabilityContentItem.class, new VulnerabilityProcessor(vulnerabilityCache, intLogger));
    // } catch (final IntegrationException ex) {
    // intLogger.error("Error building the notification processor", ex);
    // }
    // }

    public DBStoreEvent process(final NotificationResults notificationData) {
        final Collection<CommonNotificationState> notificationContentItems = notificationData.getNotificationContentItems();
        final HubBucket bucket = notificationData.getHubBucket();
        final List<NotificationModel> notificationList = new ArrayList<>(notificationContentItems.size());
        notificationContentItems.forEach(commonNotificationState -> {
            notificationList.addAll(createModels(commonNotificationState, bucket));
        });

        return new DBStoreEvent(notificationList);
    }

    private List<NotificationModel> createModels(final CommonNotificationState commonNotificationState, final HubBucket bucket) {
        final List<NotificationModel> modelList = new ArrayList<>(50);

        processorList.forEach(processor -> {
            if (processor.isApplicable(commonNotificationState)) {
                processor.process(commonNotificationState, bucket);
            }
        });

        processorList.forEach(processor -> {
            modelList.addAll(processor.getModels(bucket));
        });

        return modelList;
    }

    // private List<NotificationEntity> createNotificationEntity(final CommonNotificationState commonNotificationState, final HubBucket bucket) {
    // final List<NotificationEntity> entityList = new ArrayList<>(50);
    // final Date createdAt = commonNotificationState.getCreatedAt();
    // final NotificationType notificationType = commonNotificationState.getType();
    // final List<NotificationContentDetail> detailList = commonNotificationState.getContent().getNotificationContentDetails();
    //
    // detailList.forEach(notificationDetail -> {
    // final String eventKey = "";
    // final String projectName = notificationDetail.getProjectName();
    // final String projectVersion = notificationDetail.getProjectVersionName();
    // final String componentName = notificationDetail.getComponentName().orElse("");
    // final String componentVersion = notificationDetail.getComponentVersionName().orElse("");
    // final String policyRuleName = notificationDetail.getPolicyName().orElse("");
    // final String projectUrl = "";
    // String projectVersionUrl = "";
    // if(notificationDetail.getProjectVersion().isPresent()) {
    // projectVersionUrl = notificationDetail.getProjectVersion().get().uri;
    // }
    // String policyRuleUser = "";
    // if(notificationDetail.isPolicy() && notificationType == NotificationType.POLICY_OVERRIDE) {
    // final PolicyOverrideNotificationContent policyOverrideContent = (PolicyOverrideNotificationContent) commonNotificationState.getContent();
    // policyRuleUser = String.format("%s %s", policyOverrideContent.firstName, policyOverrideContent.lastName);
    // }
    //
    // final NotificationEntity entity = new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, policyRuleUser)
    // });
    // }
    //
    // private Collection<VulnerabilityEntity> createVulnerabilites(final CommonNotificationState commonNotificationState, final HubBucket bucket) {
    //
    // }
}
