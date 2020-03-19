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
package com.synopsys.integration.alert.provider.blackduck.collector;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonField;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonFieldAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BlackDuckPolicyOverrideCollector extends BlackDuckPolicyCollector {

    @Autowired
    public BlackDuckPolicyOverrideCollector(JsonExtractor jsonExtractor, BlackDuckProperties blackDuckProperties) {
        super(jsonExtractor, List.of(BlackDuckContent.POLICY_OVERRIDE), blackDuckProperties);
    }

    @Override
    protected Collection<ComponentItem> getComponentItems(JsonFieldAccessor jsonFieldAccessor, List<JsonField<?>> notificationFields, AlertNotificationWrapper notificationContent) {
        List<ComponentItem> items = new LinkedList<>();
        ItemOperation operation = ItemOperation.DELETE;
        List<JsonField<String>> categoryFields = getStringFields(notificationFields);

        List<JsonField<PolicyInfo>> policyFields = getFieldsOfType(notificationFields, new TypeRef<PolicyInfo>() {});
        List<PolicyInfo> policyItems = getFieldValueObjectsByLabel(jsonFieldAccessor, policyFields, BlackDuckContent.LABEL_POLICY_INFO_LIST);

        Optional<String> componentVersionName = getFieldValueObjectsByLabel(jsonFieldAccessor, categoryFields, BlackDuckContent.LABEL_COMPONENT_VERSION_NAME).stream().findFirst();
        String projectVersionUrl = getFieldValueObjectsByLabel(jsonFieldAccessor, categoryFields, BlackDuckContent.LABEL_PROJECT_VERSION_NAME + JsonField.LABEL_URL_SUFFIX).stream().findFirst().orElse("");
        String componentName = getFieldValueObjectsByLabel(jsonFieldAccessor, categoryFields, BlackDuckContent.LABEL_COMPONENT_NAME).stream().findFirst().orElse("");
        String generatedLink = getBlackDuckDataHelper().getProjectComponentQueryLink(projectVersionUrl, ProjectVersionView.COMPONENTS_LINK, componentName).orElse(null);

        Optional<LinkableItem> componentVersionItem = componentVersionName.map(name -> new LinkableItem(BlackDuckContent.LABEL_COMPONENT_VERSION_NAME, name, generatedLink));
        String linkForComponentName = (componentVersionName.isPresent()) ? null : generatedLink;
        LinkableItem componentItem = new LinkableItem(BlackDuckContent.LABEL_COMPONENT_NAME, componentName, linkForComponentName);

        Optional<String> bomComponentVersionUrl = getFieldValueObjectsByLabel(jsonFieldAccessor, categoryFields, BlackDuckContent.LABEL_BOM_COMPONENT + JsonField.LABEL_URL_SUFFIX).stream().findFirst();

        Optional<LinkableItem> nameItem = createNameItem(jsonFieldAccessor, categoryFields);

        for (PolicyInfo policyItem : policyItems) {
            ComponentItemPriority priority = getPolicyPriority(policyItem.getSeverity());
            Set<LinkableItem> attributeSet = new LinkedHashSet<>();
            attributeSet.addAll(createPolicyLinkableItems(policyItem, bomComponentVersionUrl.orElse("")));
            nameItem.ifPresent(attributeSet::add);
            Optional<ComponentItem> item = addApplicableItems(notificationContent.getId(), componentItem, componentVersionItem.orElse(null), attributeSet, operation, priority);
            item.ifPresent(items::add);
        }
        return items;
    }

    private Optional<LinkableItem> createNameItem(JsonFieldAccessor jsonFieldAccessor, List<JsonField<String>> categoryFields) {
        Optional<LinkableItem> firstName = getLinkableItemsByLabel(jsonFieldAccessor, categoryFields, BlackDuckContent.LABEL_POLICY_OVERRIDE_FIRST_NAME).stream().findFirst();
        Optional<LinkableItem> lastName = getLinkableItemsByLabel(jsonFieldAccessor, categoryFields, BlackDuckContent.LABEL_POLICY_OVERRIDE_LAST_NAME).stream().findFirst();

        if (firstName.isPresent() && lastName.isPresent()) {
            String value = String.format("%s %s", firstName.get().getValue(), lastName.get().getValue());
            LinkableItem overrideBy = new LinkableItem(BlackDuckContent.LABEL_POLICY_OVERRIDE_BY, value);
            return Optional.of(overrideBy);
        }
        return Optional.empty();
    }

}
