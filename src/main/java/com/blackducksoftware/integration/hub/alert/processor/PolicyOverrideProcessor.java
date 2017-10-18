/**
 * Copyright (C) 2017 Black Duck Software, Inc.
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

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.notification.processor.ItemTypeEnum;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.notification.processor.SubProcessorCache;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;

public class PolicyOverrideProcessor extends PolicyViolationProcessor {

    public PolicyOverrideProcessor(final SubProcessorCache cache, final MetaService metaService) {
        super(cache, metaService);
    }

    @Override
    public void process(final NotificationContentItem notification) throws HubIntegrationException {
        final PolicyOverrideContentItem policyOverrideContentItem = (PolicyOverrideContentItem) notification;
        final Map<String, Object> dataMap = new HashMap<>();
        for (final PolicyRuleView rule : policyOverrideContentItem.getPolicyRuleList()) {
            dataMap.put(POLICY_CONTENT_ITEM, policyOverrideContentItem);
            dataMap.put(POLICY_RULE, rule);
            final String eventKey = generateEventKey(dataMap);
            final Map<String, Object> dataSet = generateDataSet(dataMap);
            final NotificationEvent event = new NotificationEvent(eventKey, NotificationCategoryEnum.POLICY_VIOLATION, dataSet);
            if (getCache().hasEvent(event.getEventKey())) {
                getCache().removeEvent(event);
            } else {
                event.setCategoryType(NotificationCategoryEnum.POLICY_VIOLATION_OVERRIDE);
                getCache().addEvent(event);
            }
        }
    }

    @Override
    public Map<String, Object> generateDataSet(final Map<String, Object> inputData) {
        final PolicyOverrideContentItem policyOverride = (PolicyOverrideContentItem) inputData.get(POLICY_CONTENT_ITEM);
        final Map<String, Object> dataSet = super.generateDataSet(inputData);
        final String person = StringUtils.join(" ", policyOverride.getFirstName(), policyOverride.getLastName());
        dataSet.put(ItemTypeEnum.PERSON.name(), person);
        return dataSet;
    }

}
