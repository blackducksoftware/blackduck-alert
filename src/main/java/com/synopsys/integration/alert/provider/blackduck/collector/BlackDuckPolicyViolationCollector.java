/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.jayway.jsonpath.TypeRef;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonField;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonFieldAccessor;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentProcessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderContentTypes;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BlackDuckPolicyViolationCollector extends BlackDuckPolicyCollector {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public BlackDuckPolicyViolationCollector(final JsonExtractor jsonExtractor,
        final List<MessageContentProcessor> messageContentProcessorList) {
        super(jsonExtractor, messageContentProcessorList, Arrays.asList(BlackDuckProviderContentTypes.RULE_VIOLATION, BlackDuckProviderContentTypes.RULE_VIOLATION_CLEARED));
    }

    @Override
    protected void addCategoryItems(final List<CategoryItem> categoryItems, final JsonFieldAccessor jsonFieldAccessor, final List<JsonField<?>> notificationFields, final AlertNotificationWrapper notificationContent) {
        final ItemOperation operation = getOperationFromNotification(notificationContent);
        if (operation == null) {
            return;
        }

        final List<JsonField<PolicyInfo>> policyFields = getFieldsOfType(notificationFields, new TypeRef<PolicyInfo>() {});
        final List<JsonField<ComponentVersionStatus>> componentFields = getFieldsOfType(notificationFields, new TypeRef<ComponentVersionStatus>() {});

        final Map<String, PolicyInfo> policyItems = getFieldValueObjectsByLabel(jsonFieldAccessor, policyFields, BlackDuckProviderContentTypes.LABEL_POLICY_INFO_LIST).stream()
                                                        .collect(Collectors.toMap(PolicyInfo::getPolicy, Function.identity()));
        final List<ComponentVersionStatus> componentVersionStatuses = getFieldValueObjectsByLabel(jsonFieldAccessor, componentFields, BlackDuckProviderContentTypes.LABEL_COMPONENT_VERSION_STATUS);

        for (final ComponentVersionStatus componentVersionStatus : componentVersionStatuses) {
            final BlackDuckPolicyLinkableItem blackDuckPolicyLinkableItem = mapPolicyToComponent(componentVersionStatus);
            for (final String policyUrl : blackDuckPolicyLinkableItem.getPolicyUrls()) {
                final PolicyInfo policyItem = policyItems.get(policyUrl);
                if (null != policyItem) {
                    final String policyName = policyItem.getPolicyName();
                    final LinkableItem policyLinkableItem = new LinkableItem(BlackDuckProviderContentTypes.LABEL_POLICY_NAME, policyName, policyUrl);
                    final SortedSet<LinkableItem> applicableItems = blackDuckPolicyLinkableItem.getLinkableItems();
                    addApplicableItems(categoryItems, notificationContent.getId(), policyLinkableItem, policyUrl, operation, applicableItems);
                }
            }
        }
    }

    private ItemOperation getOperationFromNotification(final AlertNotificationWrapper notificationContent) {
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

    private BlackDuckPolicyLinkableItem mapPolicyToComponent(final ComponentVersionStatus componentVersionStatus) {
        final BlackDuckPolicyLinkableItem blackDuckPolicyLinkableItem = new BlackDuckPolicyLinkableItem();

        final String componentName = componentVersionStatus.getComponentName();
        if (StringUtils.isNotBlank(componentName)) {
            blackDuckPolicyLinkableItem.addLinkableItem(new LinkableItem(BlackDuckProviderContentTypes.LABEL_COMPONENT_NAME, componentName, componentVersionStatus.getComponent()));
        }

        final String componentVersionName = componentVersionStatus.getComponentVersionName();
        if (StringUtils.isNotBlank(componentVersionName)) {
            blackDuckPolicyLinkableItem.addLinkableItem(new LinkableItem(BlackDuckProviderContentTypes.LABEL_COMPONENT_VERSION_NAME, componentVersionName, componentVersionStatus.getComponentVersion()));
        }

        blackDuckPolicyLinkableItem.setPolicyUrls(componentVersionStatus.getPolicies().stream().collect(Collectors.toSet()));
        return blackDuckPolicyLinkableItem;
    }
}
