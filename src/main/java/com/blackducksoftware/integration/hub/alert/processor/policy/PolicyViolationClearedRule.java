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
import com.blackducksoftware.integration.hub.notification.NotificationDetailResult;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;

public class PolicyViolationClearedRule extends AbstractPolicyViolationRule {

    public PolicyViolationClearedRule(final GlobalProperties globalProperties) {
        super(globalProperties, NotificationType.RULE_VIOLATION_CLEARED);
    }

    @Override
    public void apply(final Map<String, NotificationProcessingModel> modelMap, final NotificationDetailResult notificationDetailResult, final HubBucket bucket) {
        addOrRemoveIfExists(modelMap, notificationDetailResult);
    }

    @Override
    protected NotificationProcessingModel createProcessingModel(final NotificationDetailResult notificationDetailResult) {
        return createProcessingModel(notificationDetailResult, NotificationCategoryEnum.POLICY_VIOLATION_CLEARED);
    }
}
