/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model.project;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.CombinableModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessage;

public class ProjectMessage extends ProviderMessage<ProjectMessage> {
    private final MessageReason messageReason;
    private final ProjectOperation operation;

    private final LinkableItem project;
    private final LinkableItem projectVersion;
    private final List<BomComponentDetails> bomComponents;

    public static ProjectMessage projectStatusInfo(ProviderDetails providerDetails, LinkableItem project, ProjectOperation operation) {
        return new ProjectMessage(providerDetails, MessageReason.PROJECT_STATUS, operation, project, null, List.of());
    }

    public static ProjectMessage projectVersionStatusInfo(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion, ProjectOperation operation) {
        return new ProjectMessage(providerDetails, MessageReason.PROJECT_VERSION_STATUS, operation, project, projectVersion, List.of());
    }

    public static ProjectMessage componentUpdate(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion, List<BomComponentDetails> bomComponents) {
        return new ProjectMessage(providerDetails, MessageReason.COMPONENT_UPDATE, null, project, projectVersion, bomComponents);
    }

    public static ProjectMessage componentConcern(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion, List<BomComponentDetails> bomComponents) {
        return new ProjectMessage(providerDetails, MessageReason.COMPONENT_CONCERN, null, project, projectVersion, bomComponents);
    }

    private ProjectMessage(
        ProviderDetails providerDetails,
        MessageReason messageReason,
        ProjectOperation operation,
        LinkableItem project,
        @Nullable LinkableItem projectVersion,
        List<BomComponentDetails> bomComponents
    ) {
        super(providerDetails);
        this.messageReason = messageReason;
        this.operation = operation;
        this.project = project;
        this.projectVersion = projectVersion;
        this.bomComponents = bomComponents;
    }

    public MessageReason getMessageReason() {
        return messageReason;
    }

    public Optional<ProjectOperation> getOperation() {
        return Optional.ofNullable(operation);
    }

    public LinkableItem getProject() {
        return project;
    }

    public Optional<LinkableItem> getProjectVersion() {
        return Optional.ofNullable(projectVersion);
    }

    // TODO consider making this a sorted set
    public List<BomComponentDetails> getBomComponents() {
        return bomComponents;
    }

    public boolean hasBomComponents() {
        return !bomComponents.isEmpty();
    }

    @Override
    public List<ProjectMessage> combine(ProjectMessage otherMessage) {
        List<ProjectMessage> uncombinedMessages = List.of(this, otherMessage);

        if (!getProviderDetails().equals(otherMessage.getProviderDetails())) {
            return uncombinedMessages;
        }

        if (!messageReason.equals(otherMessage.getMessageReason())) {
            return uncombinedMessages;
        }

        if (!project.equals(otherMessage.project)) {
            return uncombinedMessages;
        }

        if (messageReason.equals(MessageReason.PROJECT_STATUS)) {
            return combineOperations(otherMessage);
        }

        if (!EqualsBuilder.reflectionEquals(projectVersion, otherMessage.projectVersion)) {
            return uncombinedMessages;
        }

        if (messageReason.equals(MessageReason.PROJECT_VERSION_STATUS)) {
            return combineOperations(otherMessage);
        }

        return combineBomComponents(otherMessage.getBomComponents());
    }

    private List<ProjectMessage> combineOperations(ProjectMessage otherMessage) {
        if (ProjectOperation.CREATE.equals(operation) && ProjectOperation.DELETE.equals(otherMessage.operation)) {
            return List.of();
        } else if (ProjectOperation.DELETE.equals(operation) && ProjectOperation.CREATE.equals(otherMessage.operation)) {
            return List.of(otherMessage);
        }
        return List.of(this);
    }

    private List<ProjectMessage> combineBomComponents(List<BomComponentDetails> otherMessageBomComponents) {
        List<BomComponentDetails> combinedBomComponents = CombinableModel.combine(bomComponents, otherMessageBomComponents);
        ProjectMessage projectMessageWithCombinedComponents = new ProjectMessage(getProviderDetails(), messageReason, operation, project, projectVersion, combinedBomComponents);
        return List.of(projectMessageWithCombinedComponents);
    }

}
