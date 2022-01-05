/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.distribution;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.email.attachment.compatibility.ComponentItem;
import com.synopsys.integration.alert.channel.email.attachment.compatibility.MessageContentGroup;
import com.synopsys.integration.alert.channel.email.attachment.compatibility.ProviderMessageContent;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentUpgradeGuidance;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectOperation;

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
