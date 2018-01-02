/**
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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

import java.util.HashMap;
import java.util.Map;

import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.notification.processor.SubProcessorCache;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
import com.blackducksoftware.integration.log.IntLogger;

public class PolicyViolationClearedProcessor extends PolicyViolationProcessor {

    public PolicyViolationClearedProcessor(final SubProcessorCache cache, final IntLogger intLogger) {
        super(cache, intLogger);
    }

    @Override
    public void process(final NotificationContentItem notification) throws HubIntegrationException {
        if (notification instanceof PolicyViolationClearedContentItem) {
            final PolicyViolationClearedContentItem policyViolationCleared = (PolicyViolationClearedContentItem) notification;
            final Map<String, Object> dataMap = new HashMap<>();
            for (final PolicyRuleView rule : policyViolationCleared.getPolicyRuleList()) {
                dataMap.put(POLICY_CONTENT_ITEM, policyViolationCleared);
                dataMap.put(POLICY_RULE, rule);
                final String eventKey = generateEventKey(dataMap);
                final Map<String, Object> dataSet = generateDataSet(dataMap);
                final NotificationEvent event = new NotificationEvent(eventKey, NotificationCategoryEnum.POLICY_VIOLATION, dataSet);
                if (getCache().hasEvent(event.getEventKey())) {
                    getCache().removeEvent(event);
                } else {
                    event.setCategoryType(NotificationCategoryEnum.POLICY_VIOLATION_CLEARED);
                    getCache().addEvent(event);
                }
            }
        }
    }
}
