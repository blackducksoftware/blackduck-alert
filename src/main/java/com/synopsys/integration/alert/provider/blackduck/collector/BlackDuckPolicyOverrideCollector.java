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
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonField;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonFieldAccessor;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentProcessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BlackDuckPolicyOverrideCollector extends BlackDuckPolicyCollector {

    @Autowired
    public BlackDuckPolicyOverrideCollector(final JsonExtractor jsonExtractor, final List<MessageContentProcessor> messageContentProcessorList, final BlackDuckProperties blackDuckProperties) {
        super(jsonExtractor, messageContentProcessorList, Arrays.asList(BlackDuckContent.POLICY_OVERRIDE), blackDuckProperties);
    }

    @Override
    protected void addCategoryItems(final SortedSet<CategoryItem> categoryItems, final JsonFieldAccessor jsonFieldAccessor, final List<JsonField<?>> notificationFields, final AlertNotificationWrapper notificationContent) {
        final ItemOperation operation = ItemOperation.DELETE;
        final List<JsonField<String>> categoryFields = getStringFields(notificationFields);
        final List<LinkableItem> policyItems = getItemsByLabel(jsonFieldAccessor, categoryFields, BlackDuckContent.LABEL_POLICY_NAME);
        policyItems.forEach(policyItem -> policyItem.setCollapsible(true));
        final List<LinkableItem> policySeverity = getLinkableItemsByLabel(jsonFieldAccessor, categoryFields, BlackDuckContent.LABEL_POLICY_SEVERITY_NAME);
        policySeverity.forEach(severityItem -> severityItem.setSummarizable(true));

        final Optional<LinkableItem> firstName = getLinkableItemsByLabel(jsonFieldAccessor, categoryFields, BlackDuckContent.LABEL_POLICY_OVERRIDE_FIRST_NAME).stream().findFirst();
        final Optional<LinkableItem> lastName = getLinkableItemsByLabel(jsonFieldAccessor, categoryFields, BlackDuckContent.LABEL_POLICY_OVERRIDE_LAST_NAME).stream().findFirst();

        final Optional<String> componentVersionName = getFieldValueObjectsByLabel(jsonFieldAccessor, categoryFields, BlackDuckContent.LABEL_COMPONENT_VERSION_NAME).stream().findFirst();
        final String projectVersionUrl = getFieldValueObjectsByLabel(jsonFieldAccessor, categoryFields, BlackDuckContent.LABEL_PROJECT_VERSION_NAME + JsonField.LABEL_URL_SUFFIX).stream().findFirst().orElse("");
        final String componentName = getFieldValueObjectsByLabel(jsonFieldAccessor, categoryFields, BlackDuckContent.LABEL_COMPONENT_NAME).stream().findFirst().orElse("");
        final String generatedLink = getProjectQueryLink(projectVersionUrl, ProjectVersionView.COMPONENTS_LINK, componentName).orElse(null);

        final Optional<LinkableItem> componentVersionItem = componentVersionName.map(name -> new LinkableItem(BlackDuckContent.LABEL_COMPONENT_VERSION_NAME, name, generatedLink));
        final String linkForComponentName = (componentVersionName.isPresent()) ? null : generatedLink;
        final LinkableItem componentItem = new LinkableItem(BlackDuckContent.LABEL_COMPONENT_NAME, componentName, linkForComponentName);

        final SortedSet<LinkableItem> applicableItems = new TreeSet<>();
        applicableItems.add(componentItem);
        applicableItems.addAll(policySeverity);
        componentVersionItem.ifPresent(applicableItems::add);

        if (firstName.isPresent() && lastName.isPresent()) {
            final String value = String.format("%s %s", firstName.get().getValue(), lastName.get().getValue());
            final LinkableItem nameItem = new LinkableItem(BlackDuckContent.LABEL_POLICY_OVERRIDE_BY, value);
            applicableItems.add(nameItem);
        }

        for (final LinkableItem policyItem : policyItems) {
            addApplicableItems(categoryItems, notificationContent.getId(), Set.of(policyItem), operation, applicableItems);
        }
    }

}
