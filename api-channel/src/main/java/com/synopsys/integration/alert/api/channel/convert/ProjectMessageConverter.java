/*
 * api-channel
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.convert;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.channel.message.ChunkedStringBuilder;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.MessageReason;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

public class ProjectMessageConverter implements ProviderMessageConverter<ProjectMessage> {
    private final ChannelMessageFormatter messageFormatter;
    private final LinkableItemConverter linkableItemConverter;
    private final BomComponentDetailConverter bomComponentDetailConverter;
    private final ComponentConcernConverter componentConcernConverter;

    public ProjectMessageConverter(ChannelMessageFormatter formatter) {
        this.messageFormatter = formatter;
        this.linkableItemConverter = new LinkableItemConverter(messageFormatter);
        this.bomComponentDetailConverter = new BomComponentDetailConverter(formatter);
        this.componentConcernConverter = new ComponentConcernConverter(formatter);
    }

    @Override
    public List<String> convertToFormattedMessageChunks(ProjectMessage projectMessage, String jobName) {
        ChunkedStringBuilder chunkedStringBuilder = new ChunkedStringBuilder(messageFormatter.getMaxMessageLength());

        String projectString;
        Optional<String> optionalProjectVersionString;
        Optional<LinkableItem> optionalProjectVersion = projectMessage.getProjectVersion();
        if (optionalProjectVersion.isPresent()) {
            projectString = linkableItemConverter.convertToStringWithoutLink(projectMessage.getProject(), true);
            optionalProjectVersionString = optionalProjectVersion.map(projectVersion -> linkableItemConverter.convertToString(projectVersion, true));
        } else {
            projectString = linkableItemConverter.convertToString(projectMessage.getProject(), true);
            optionalProjectVersionString = Optional.empty();
        }

        String nonBreakingSpace = messageFormatter.getNonBreakingSpace();

        String jobLine = String.format("Job%sname:%s%s", nonBreakingSpace, nonBreakingSpace, jobName);
        String boldJobName = messageFormatter.emphasize(jobLine);
        chunkedStringBuilder.append(boldJobName);
        chunkedStringBuilder.append(messageFormatter.getLineSeparator());

        chunkedStringBuilder.append(projectString);
        chunkedStringBuilder.append(messageFormatter.getLineSeparator());

        optionalProjectVersionString
            .ifPresent(projectVersionString -> {
                chunkedStringBuilder.append(projectVersionString);
                chunkedStringBuilder.append(messageFormatter.getLineSeparator());
            });

        MessageReason messageReason = projectMessage.getMessageReason();
        if (MessageReason.PROJECT_STATUS.equals(messageReason) || MessageReason.PROJECT_VERSION_STATUS.equals(messageReason)) {
            projectMessage.getOperation()
                .map(operation -> String.format("Project%sAction:%s%s", nonBreakingSpace, nonBreakingSpace, operation.name()))
                .map(messageFormatter::encode)
                .ifPresent(chunkedStringBuilder::append);
            return chunkedStringBuilder.collectCurrentChunks();
        }

        List<BomComponentDetails> bomComponents = projectMessage.getBomComponents();
        if (!bomComponents.isEmpty()) {
            chunkedStringBuilder.append(messageFormatter.getSectionSeparator());
            chunkedStringBuilder.append(messageFormatter.getLineSeparator());
        }

        for (BomComponentDetails bomComponentDetails : bomComponents) {
            List<String> bomComponentMessagePieces = gatherBomComponentAndConcernSectionPieces(bomComponentDetails);
            bomComponentMessagePieces.forEach(chunkedStringBuilder::append);
            chunkedStringBuilder.append(messageFormatter.getSectionSeparator());
            chunkedStringBuilder.append(messageFormatter.getLineSeparator());
        }

        return chunkedStringBuilder.collectCurrentChunks();
    }

    private List<String> gatherBomComponentAndConcernSectionPieces(BomComponentDetails bomComponent) {
        List<String> bomComponentSectionPieces = new LinkedList<>();

        List<String> preConcernSectionPieces = bomComponentDetailConverter.gatherAbstractBomComponentSectionPieces(bomComponent);
        bomComponentSectionPieces.addAll(preConcernSectionPieces);

        List<String> componentConcernSectionPieces = componentConcernConverter.gatherComponentConcernSectionPieces(bomComponent.getComponentConcerns());
        bomComponentSectionPieces.addAll(componentConcernSectionPieces);

        return bomComponentSectionPieces;
    }

}
