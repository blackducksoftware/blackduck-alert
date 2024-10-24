/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.distribution;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.processor.extract.model.project.BomComponentDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcern;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernType;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentUpgradeGuidance;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectOperation;
import com.blackduck.integration.alert.channel.email.attachment.compatibility.ComponentItem;
import com.blackduck.integration.alert.channel.email.attachment.compatibility.MessageContentGroup;
import com.blackduck.integration.alert.channel.email.attachment.compatibility.ProviderMessageContent;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

// TODO remove this in 7.0.0
public final class ProjectMessageToMessageContentGroupConversionUtils {
    public static MessageContentGroup toMessageContentGroup(ProjectMessage projectMessage) {
        ProviderMessageContent.Builder providerMessageContentBuilder = new ProviderMessageContent.Builder();

        LinkableItem provider = projectMessage.getProvider();
        providerMessageContentBuilder.applyProvider(provider.getLabel(), -1L, provider.getValue(), provider.getUrl().orElse(null));

        LinkableItem project = projectMessage.getProject();
        providerMessageContentBuilder.applyTopic(project.getLabel(), project.getValue(), project.getUrl().orElse(null));

        Optional<LinkableItem> optionalProjectVersion = projectMessage.getProjectVersion();
        if (optionalProjectVersion.isPresent()) {
            LinkableItem projectVersion = optionalProjectVersion.get();
            providerMessageContentBuilder.applySubTopic(projectVersion.getLabel(), projectVersion.getValue(), projectVersion.getUrl().orElse(null));
        }

        projectMessage.getOperation()
            .map(ProjectMessageToMessageContentGroupConversionUtils::convertToItemOperation)
            .ifPresent(providerMessageContentBuilder::applyAction);

        List<ComponentItem> componentItems = new LinkedList<>();
        for (BomComponentDetails bomComponent : projectMessage.getBomComponents()) {
            List<ComponentItem> bomComponentItems = convertToComponentItems(bomComponent);
            componentItems.addAll(bomComponentItems);
        }
        providerMessageContentBuilder.applyAllComponentItems(componentItems);

        MessageContentGroup messageContentGroup = new MessageContentGroup();
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
            bomComponent.getComponentVersion().ifPresent(componentItemBuilder::applySubComponent);

            String categoryItemLabel = convertToCategoryLabel(type);
            LinkableItem categoryItem = new LinkableItem(categoryItemLabel, componentConcern.getName(), componentConcern.getUrl().orElse(null));
            componentItemBuilder.applyCategoryItem(categoryItem);

            String severity = componentConcern.getSeverity().name();
            componentItemBuilder.applyCategoryGroupingAttribute("Severity", severity);

            boolean collapseOnCategory = ComponentConcernType.VULNERABILITY.equals(type);
            componentItemBuilder.applyCollapseOnCategory(collapseOnCategory);

            List<LinkableItem> componentAttributes = new LinkedList<>();
            componentAttributes.add(bomComponent.getLicense());

            LinkableItem usageItem = new LinkableItem("Usage", bomComponent.getUsage());
            componentAttributes.add(usageItem);

            ComponentUpgradeGuidance upgradeGuidance = bomComponent.getComponentUpgradeGuidance();
            upgradeGuidance.getLongTermUpgradeGuidance().ifPresent(componentAttributes::add);
            upgradeGuidance.getShortTermUpgradeGuidance().ifPresent(componentAttributes::add);

            componentAttributes.addAll(bomComponent.getAdditionalAttributes());
            componentItemBuilder.applyAllComponentAttributes(componentAttributes);

            try {
                componentItems.add(componentItemBuilder.build());
            } catch (AlertException e) {
                // Ignored for feature parity
            }
        }

        return componentItems;
    }

    private static ItemOperation convertToItemOperation(ProjectOperation projectOperation) {
        if (ProjectOperation.CREATE.equals(projectOperation)) {
            return ItemOperation.ADD;
        } else {
            return ItemOperation.DELETE;
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
