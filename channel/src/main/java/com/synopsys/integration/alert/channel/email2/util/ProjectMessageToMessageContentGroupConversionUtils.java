/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.email2.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectOperation;

// TODO remove this in 7.0.0
public final class ProjectMessageToMessageContentGroupConversionUtils {
    public static MessageContentGroup toMessageContentGroup(ProjectMessage projectMessage) {
        ProviderMessageContent.Builder providerMessageContentBuilder = new ProviderMessageContent.Builder();

        LinkableItem provider = projectMessage.getProvider();
        providerMessageContentBuilder.applyProvider(provider.getLabel(), -1L, provider.getValue(), provider.getUrl().orElse(null));

        LinkableItem project = projectMessage.getProject();
        providerMessageContentBuilder.applyProject(project.getLabel(), project.getValue(), project.getUrl().orElse(null));

        Optional<LinkableItem> optionalProjectVersion = projectMessage.getProjectVersion();
        if (optionalProjectVersion.isPresent()) {
            LinkableItem projectVersion = optionalProjectVersion.get();
            providerMessageContentBuilder.applyProjectVersion(projectVersion.getLabel(), projectVersion.getValue(), projectVersion.getUrl().orElse(null));
        }

        projectMessage.getOperation()
            .map(ProjectMessageToMessageContentGroupConversionUtils::convertToItemOperation)
            .ifPresent(providerMessageContentBuilder::applyAction);

        MessageContentGroup messageContentGroup = new MessageContentGroup();
        List<ComponentItem> componentItems = new LinkedList<>();
        for (BomComponentDetails bomComponent : projectMessage.getBomComponents()) {
            List<ComponentItem> bomComponentItems = convertToComponentItems(bomComponent);
            componentItems.addAll(bomComponentItems);
        }

        providerMessageContentBuilder.applyAllComponentItems(componentItems);
        try {
            ProviderMessageContent providerMessageContent = providerMessageContentBuilder.build();
            messageContentGroup.add(providerMessageContent);
        } catch (AlertException e) {
            // Ignore for feature parity
        }
        return messageContentGroup;
    }

    private static List<ComponentItem> convertToComponentItems(BomComponentDetails bomComponent) {
        List<ComponentItem> componentItems = new LinkedList<>();
        for (ComponentConcern componentConcern : bomComponent.getComponentConcerns()) {
            ComponentItem.Builder componentItemBuilder = new ComponentItem.Builder();

            componentItemBuilder.applyOperation(componentConcern.getOperation());

            ComponentConcernType type = componentConcern.getType();
            String category = StringUtils.capitalize(StringUtils.lowerCase(type.name()));
            componentItemBuilder.applyCategory(category);

            componentItemBuilder.applyComponentData(bomComponent.getComponent());
            bomComponent.getComponentVersion().ifPresent(componentItemBuilder::applyComponentVersion);

            String categoryItemLabel = convertToCategoryLabel(type);
            LinkableItem categoryItem = new LinkableItem(categoryItemLabel, componentConcern.getName(), componentConcern.getUrl().orElse(null));
            componentItemBuilder.applyCategoryItem(categoryItem);

            String severity = componentConcern.getSeverity().name();
            componentItemBuilder.applySeverity("Severity", severity);

            boolean collapseOnCategory = ComponentConcernType.VULNERABILITY.equals(type);
            componentItemBuilder.applyCollapseOnCategory(collapseOnCategory);

            List<LinkableItem> componentAttributes = new LinkedList<>();
            componentAttributes.add(bomComponent.getLicense());

            LinkableItem usageItem = new LinkableItem("Usage", bomComponent.getUsage());
            componentAttributes.add(usageItem);

            componentAttributes.addAll(bomComponent.getAdditionalAttributes());

            try {
                componentItems.add(componentItemBuilder.build());
            } catch (AlertException e) {
                // Ignored for feature parity
            }
        }

        return componentItems;
    }

    private static ItemOperation convertToItemOperation(ProjectOperation projectOperation) {
        switch (projectOperation) {
            case CREATE:
                return ItemOperation.ADD;
            case DELETE:
                return ItemOperation.DELETE;
            default:
                return ItemOperation.UPDATE;
        }
    }

    private static String convertToCategoryLabel(ComponentConcernType type) {
        if (ComponentConcernType.POLICY.equals(type)) {
            return "Policy Violated";
        } else {
            return "Vulnerabilities";
        }
    }

    private ProjectMessageToMessageContentGroupConversionUtils() {
    }

}
