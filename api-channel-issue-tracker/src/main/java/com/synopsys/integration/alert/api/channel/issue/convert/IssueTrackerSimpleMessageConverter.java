/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.convert;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.api.channel.convert.SimpleMessageConverter;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.common.channel.message.ChunkedStringBuilderRechunker;
import com.synopsys.integration.alert.common.channel.message.RechunkedModel;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;

public class IssueTrackerSimpleMessageConverter {
    private final IssueTrackerMessageFormatter formatter;
    private final SimpleMessageConverter simpleMessageConverter;

    public IssueTrackerSimpleMessageConverter(IssueTrackerMessageFormatter formatter) {
        this.formatter = formatter;
        this.simpleMessageConverter = new SimpleMessageConverter(formatter);
    }

    public IssueCreationModel convertToIssueCreationModel(SimpleMessage simpleMessage, String jobName) {
        LinkableItem provider = simpleMessage.getProvider();
        String rawTitle = String.format("%s[%s] | %s", provider.getLabel(), provider.getValue(), simpleMessage.getSummary());
        String truncatedTitle = StringUtils.truncate(rawTitle, formatter.getMaxTitleLength());

        List<String> descriptionChunks = simpleMessageConverter.convertToFormattedMessageChunks(simpleMessage, jobName);
        RechunkedModel rechunkedDescription = ChunkedStringBuilderRechunker.rechunk(descriptionChunks, "No description", formatter.getMaxDescriptionLength(), formatter.getMaxCommentLength());

        return IssueCreationModel.simple(truncatedTitle, rechunkedDescription.getFirstChunk(), rechunkedDescription.getRemainingChunks(), simpleMessage.getProvider());
    }

}
