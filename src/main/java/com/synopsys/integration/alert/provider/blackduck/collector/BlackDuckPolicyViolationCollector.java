/**
 * blackduck-alert
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
package com.synopsys.integration.alert.provider.blackduck.collector;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.jayway.jsonpath.TypeRef;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.field.JsonField;
import com.synopsys.integration.alert.common.model.CategoryItem;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentProcessor;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderContentTypes;
import com.synopsys.integration.alert.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.workflow.filter.field.JsonFieldAccessor;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.notification.content.ComponentVersionStatus;
import com.synopsys.integration.blackduck.notification.content.PolicyInfo;

@Component
@Scope("prototype")
public class BlackDuckPolicyViolationCollector extends BlackDuckPolicyCollector {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public BlackDuckPolicyViolationCollector(final JsonExtractor jsonExtractor,
        final List<MessageContentProcessor> messageContentProcessorList) {
        super(jsonExtractor, messageContentProcessorList, Arrays.asList(BlackDuckProviderContentTypes.RULE_VIOLATION, BlackDuckProviderContentTypes.RULE_VIOLATION_CLEARED));
    }

    @Override
    protected void addCategoryItems(final List<CategoryItem> categoryItems, final JsonFieldAccessor jsonFieldAccessor, final List<JsonField<?>> notificationFields, final NotificationContent notificationContent) {
        final ItemOperation operation = getOperationFromNotification(notificationContent);
        if (operation == null) {
            return;
        }

        final List<JsonField<PolicyInfo>> policyFields = getFieldsOfType(notificationFields, new TypeRef<PolicyInfo>() {});
        final List<JsonField<ComponentVersionStatus>> componentFields = getFieldsOfType(notificationFields, new TypeRef<ComponentVersionStatus>() {});
        try {
            final List<PolicyInfo> policyItems = getFieldValueObjectsByLabel(jsonFieldAccessor, policyFields, BlackDuckProviderContentTypes.LABEL_POLICY_INFO_LIST);
            final List<ComponentVersionStatus> componentVersionStatuses = getFieldValueObjectsByLabel(jsonFieldAccessor, componentFields, BlackDuckProviderContentTypes.LABEL_COMPONENT_VERSION_STATUS);
            final Map<String, SortedSet<LinkableItem>> policyItemMap = mapPolicyToComponents(componentVersionStatuses);

            for (final PolicyInfo policyItem : policyItems) {
                final String policyUrl = policyItem.policy;
                final String policyName = policyItem.policyName;
                final LinkableItem policyLinkableItem = new LinkableItem(BlackDuckProviderContentTypes.LABEL_POLICY_NAME, policyName, policyUrl);
                if (policyItemMap.containsKey(policyUrl)) {
                    final SortedSet<LinkableItem> applicableItems = policyItemMap.get(policyUrl);
                    addApplicableItems(categoryItems, notificationContent.getId(), policyLinkableItem, policyUrl, operation, applicableItems);
                }
            }
        } catch (final AlertException ex) {
            logger.error("Mishandled the expected type of a notification field", ex);
        }
    }

    private ItemOperation getOperationFromNotification(final NotificationContent notificationContent) {
        final ItemOperation operation;
        final String notificationType = notificationContent.getNotificationType();
        if (NotificationType.RULE_VIOLATION_CLEARED.name().equals(notificationType)) {
            operation = ItemOperation.DELETE;
        } else if (NotificationType.RULE_VIOLATION.name().equals(notificationType)) {
            operation = ItemOperation.ADD;
        } else {
            operation = null;
            logger.error("Unrecognized notification type: The notification type '{}' is not valid for this collector.", notificationType);
        }

        return operation;
    }

    private Map<String, SortedSet<LinkableItem>> mapPolicyToComponents(final List<ComponentVersionStatus> componentVersionStatuses) {
        final Map<String, SortedSet<LinkableItem>> policyItemMap = new TreeMap<>();

        for (final ComponentVersionStatus versionStatus : componentVersionStatuses) {
            for (final String policyUrl : versionStatus.policies) {
                final SortedSet<LinkableItem> componentItems;
                if (policyItemMap.containsKey(policyUrl)) {
                    componentItems = policyItemMap.get(policyUrl);
                } else {
                    componentItems = new TreeSet<>();
                    policyItemMap.put(policyUrl, componentItems);
                }

                if (StringUtils.isNotBlank(versionStatus.componentName)) {
                    componentItems.add(new LinkableItem(BlackDuckProviderContentTypes.LABEL_COMPONENT_NAME, versionStatus.componentName, versionStatus.component));
                }

                if (StringUtils.isNotBlank(versionStatus.componentVersionName)) {
                    componentItems.add(new LinkableItem(BlackDuckProviderContentTypes.LABEL_COMPONENT_VERSION_NAME, versionStatus.componentVersionName, versionStatus.componentVersion));
                }
            }
        }
        return policyItemMap;
    }
}
