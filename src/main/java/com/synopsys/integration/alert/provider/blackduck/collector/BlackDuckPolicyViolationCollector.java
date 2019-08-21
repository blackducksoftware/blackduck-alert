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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import com.synopsys.integration.alert.common.enumeration.ComponentItemPriority;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonField;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonFieldAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.collector.item.BlackDuckPolicyLinkableItem;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BlackDuckPolicyViolationCollector extends BlackDuckPolicyCollector {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public BlackDuckPolicyViolationCollector(JsonExtractor jsonExtractor, BlackDuckProperties blackDuckProperties) {
        super(jsonExtractor, Arrays.asList(BlackDuckContent.RULE_VIOLATION, BlackDuckContent.RULE_VIOLATION_CLEARED), blackDuckProperties);
    }

    @Override
    protected Collection<ComponentItem> getComponentItems(JsonFieldAccessor jsonFieldAccessor, List<JsonField<?>> notificationFields, AlertNotificationWrapper notificationContent) {
        final ItemOperation operation = getOperationFromNotification(notificationContent);
        if (operation == null) {
            return List.of();
        }
        List<ComponentItem> items = new LinkedList<>();

        List<JsonField<PolicyInfo>> policyFields = getFieldsOfType(notificationFields, new TypeRef<PolicyInfo>() {});
        List<JsonField<ComponentVersionStatus>> componentFields = getFieldsOfType(notificationFields, new TypeRef<ComponentVersionStatus>() {});
        List<JsonField<String>> stringFields = getStringFields(notificationFields);

        Map<String, PolicyInfo> policyItems = getFieldValueObjectsByLabel(jsonFieldAccessor, policyFields, BlackDuckContent.LABEL_POLICY_INFO_LIST).stream()
                                                  .collect(Collectors.toMap(PolicyInfo::getPolicy, Function.identity()));
        List<ComponentVersionStatus> componentVersionStatuses = getFieldValueObjectsByLabel(jsonFieldAccessor, componentFields, BlackDuckContent.LABEL_COMPONENT_VERSION_STATUS);
        String projectVersionUrl = getFieldValueObjectsByLabel(jsonFieldAccessor, stringFields, BlackDuckContent.LABEL_PROJECT_VERSION_NAME + JsonField.LABEL_URL_SUFFIX)
                                       .stream()
                                       .findFirst()
                                       .orElse("");

        Optional<String> projectVersionComponentLink = getBlackDuckDataHelper().getProjectLink(projectVersionUrl, ProjectVersionView.COMPONENTS_LINK);

        Map<PolicyComponentMapping, BlackDuckPolicyLinkableItem> policyComponentToLinkableItemMapping = createPolicyComponentToLinkableItemMapping(componentVersionStatuses, policyItems, projectVersionComponentLink);
        for (Map.Entry<PolicyComponentMapping, BlackDuckPolicyLinkableItem> policyComponentToLinkableItem : policyComponentToLinkableItemMapping.entrySet()) {
            PolicyComponentMapping policyComponentMapping = policyComponentToLinkableItem.getKey();
            BlackDuckPolicyLinkableItem policyComponentData = policyComponentToLinkableItem.getValue();

            for (PolicyInfo policyInfo : policyComponentMapping.getPolicies()) {
                ComponentItemPriority priority = mapSeverityToPriority(policyInfo.getSeverity());

                String bomComponentUrl = null;
                ComponentVersionStatus componentVersionStatus = policyComponentData.getComponentVersionStatus();
                if (null != componentVersionStatus) {
                    bomComponentUrl = componentVersionStatus.getBomComponent();
                }
                Collection<LinkableItem> policyLinkableItems = createPolicyLinkableItems(policyInfo, bomComponentUrl);

                Optional<ComponentItem> item = addApplicableItems(notificationContent.getId(), policyComponentData.getComponentItem().orElse(null), policyComponentData.getComponentVersion().orElse(null),
                    policyLinkableItems, operation, priority);
                item.ifPresent(items::add);
            }
        }
        return items;
    }

    private ItemOperation getOperationFromNotification(AlertNotificationWrapper notificationContent) {
        ItemOperation operation;
        String notificationType = notificationContent.getNotificationType();
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

    private Map<PolicyComponentMapping, BlackDuckPolicyLinkableItem> createPolicyComponentToLinkableItemMapping(
        Collection<ComponentVersionStatus> componentVersionStatuses, Map<String, PolicyInfo> policyItems, Optional<String> projectVersionUrl) {
        Map<PolicyComponentMapping, BlackDuckPolicyLinkableItem> policyComponentToLinkableItemMapping = new HashMap<>();
        for (ComponentVersionStatus componentVersionStatus : componentVersionStatuses) {
            String projectVersionLink = projectVersionUrl.flatMap(url -> getBlackDuckDataHelper().getProjectComponentQueryLink(url, componentVersionStatus.getComponentName())).orElse(null);
            PolicyComponentMapping policyComponentMapping = createPolicyComponentMapping(componentVersionStatus, policyItems);
            BlackDuckPolicyLinkableItem blackDuckPolicyLinkableItem = policyComponentToLinkableItemMapping.get(policyComponentMapping);
            if (blackDuckPolicyLinkableItem == null) {
                blackDuckPolicyLinkableItem = createBlackDuckPolicyLinkableItem(componentVersionStatus, projectVersionLink);
            } else {
                blackDuckPolicyLinkableItem.addComponentVersionItem(componentVersionStatus.getComponentVersionName(), projectVersionLink);
                blackDuckPolicyLinkableItem.setComponentVersionStatus(componentVersionStatus);
            }
            policyComponentToLinkableItemMapping.put(policyComponentMapping, blackDuckPolicyLinkableItem);
        }
        return policyComponentToLinkableItemMapping;
    }

    private PolicyComponentMapping createPolicyComponentMapping(ComponentVersionStatus componentVersionStatus, Map<String, PolicyInfo> policyItems) {
        String componentName = componentVersionStatus.getComponentName();

        Set<PolicyInfo> policies = componentVersionStatus.getPolicies().stream()
                                       .filter(policyItems::containsKey)
                                       .map(policyItems::get)
                                       .collect(Collectors.toSet());

        return new PolicyComponentMapping(componentName, policies);
    }

    private BlackDuckPolicyLinkableItem createBlackDuckPolicyLinkableItem(ComponentVersionStatus componentVersionStatus, String projectVersionWithComponentLink) {
        BlackDuckPolicyLinkableItem blackDuckPolicyLinkableItem = new BlackDuckPolicyLinkableItem();

        String componentVersionName = componentVersionStatus.getComponentVersionName();
        if (StringUtils.isNotBlank(componentVersionName)) {
            blackDuckPolicyLinkableItem.addComponentVersionItem(componentVersionName, projectVersionWithComponentLink);
            blackDuckPolicyLinkableItem.setComponentVersionStatus(componentVersionStatus);
        }

        String componentName = componentVersionStatus.getComponentName();
        if (StringUtils.isNotBlank(componentName)) {
            String componentLink = (StringUtils.isBlank(componentVersionName)) ? projectVersionWithComponentLink : null;
            blackDuckPolicyLinkableItem.addComponentNameItem(componentName, componentLink);
        }

        return blackDuckPolicyLinkableItem;
    }

    private class PolicyComponentMapping extends AlertSerializableModel {
        // Do not delete this member. This is used for checking equals and filtering.
        private final String componentName;
        private final Set<PolicyInfo> policies;

        public PolicyComponentMapping(String componentName, Set<PolicyInfo> policies) {
            this.componentName = componentName;
            this.policies = policies;
        }

        public Set<PolicyInfo> getPolicies() {
            return policies;
        }

    }

}
