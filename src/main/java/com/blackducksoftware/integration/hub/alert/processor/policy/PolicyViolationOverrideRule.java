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

import java.util.Map;

import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.alert.processor.NotificationProcessingModel;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.notification.NotificationContentDetailResults;
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetail;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;

public class PolicyViolationOverrideRule extends AbstractPolicyViolationRule {

    public PolicyViolationOverrideRule(final GlobalProperties globalProperties) {
        super(globalProperties, NotificationType.POLICY_OVERRIDE);
    }

    @Override
    public void apply(final Map<String, NotificationProcessingModel> modelMap, final CommonNotificationState commonNotificationState, final HubBucket bucket, final NotificationContentDetailResults detailResults) {
        addOrRemoveIfExists(modelMap, commonNotificationState, detailResults);
    }

    @Override
    protected NotificationProcessingModel createProcessingModel(final CommonNotificationState commonNotificationState, final NotificationContentDetail notificationContentDetail) {
        return createProcessingModel(commonNotificationState, notificationContentDetail, NotificationCategoryEnum.POLICY_VIOLATION_OVERRIDE);
    }
}
