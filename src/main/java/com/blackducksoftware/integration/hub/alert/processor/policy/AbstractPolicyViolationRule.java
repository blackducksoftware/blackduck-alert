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
package com.blackducksoftware.integration.hub.alert.processor.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.alert.processor.NotificationProcessingModel;
import com.blackducksoftware.integration.hub.alert.processor.NotificationProcessingRule;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.notification.NotificationViewResult;
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetail;

public abstract class AbstractPolicyViolationRule extends NotificationProcessingRule<NotificationProcessingModel> {

    public AbstractPolicyViolationRule(final GlobalProperties globalProperties, final NotificationType notificationType) {
        super(globalProperties, notificationType);
    }

    public List<NotificationProcessingModel> createProcessingModels(final NotificationViewResult notificationViewResult) {
        final List<NotificationProcessingModel> modelList = new ArrayList<>();
        final List<NotificationContentDetail> contentDetails = notificationViewResult.getNotificationContentDetails();
        contentDetails.forEach(contentDetail -> {
            modelList.add(createProcessingModel(notificationViewResult.getCommonNotificationState(), contentDetail));
        });
        return modelList;
    }

    protected void addOrRemoveIfExists(final Map<String, NotificationProcessingModel> modelMap, final NotificationViewResult notificationViewResult) {
        final List<String> keyList = getContentDetailKeys(notificationViewResult);
        for (final String key : keyList) {
            if (modelMap.containsKey(key)) {
                modelMap.remove(key);
            } else {
                final List<NotificationContentDetail> detailList = notificationViewResult.getNotificationContentDetails();
                final List<NotificationContentDetail> filteredList = detailList.stream().filter(detail -> {
                    return detail.getContentDetailKey().equals(key);
                }).collect(Collectors.toList());

                if (!filteredList.isEmpty()) {
                    final NotificationContentDetail contentDetail = filteredList.get(0);
                    final NotificationProcessingModel model = createProcessingModel(notificationViewResult.getCommonNotificationState(), contentDetail);
                    modelMap.put(key, model);
                }
            }
        }
    }

    public List<String> getContentDetailKeys(final NotificationViewResult notificationViewResult) {
        final List<NotificationContentDetail> contentDetailList = notificationViewResult.getNotificationContentDetails();
        final List<String> contentKeyList = contentDetailList.stream().map(NotificationContentDetail::getContentDetailKey).collect(Collectors.toList());
        return contentKeyList;
    }

    protected NotificationProcessingModel createProcessingModel(final CommonNotificationState commonNotificationState, final NotificationContentDetail notificationContentDetail, final NotificationCategoryEnum notificationType) {
        return new NotificationProcessingModel(notificationContentDetail, commonNotificationState, notificationType);
    }

    protected abstract NotificationProcessingModel createProcessingModel(final CommonNotificationState commonNotificationState, final NotificationContentDetail notificationContentDetail);
}
