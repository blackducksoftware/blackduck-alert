/*
 * api-channel
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.convert;

import java.util.List;

import com.synopsys.integration.alert.common.channel.message.ChunkedStringBuilder;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;

public class SimpleMessageConverter implements ProviderMessageConverter<SimpleMessage> {
    private final ChannelMessageFormatter messageFormatter;
    private final LinkableItemConverter linkableItemConverter;

    public SimpleMessageConverter(ChannelMessageFormatter messageFormatter) {
        this.messageFormatter = messageFormatter;
        this.linkableItemConverter = new LinkableItemConverter(messageFormatter);
    }

    @Override
    public List<String> convertToFormattedMessageChunks(SimpleMessage simpleMessage, String jobName) {
        ChunkedStringBuilder chunkedStringBuilder = new ChunkedStringBuilder(messageFormatter.getMaxMessageLength());

        String nonBreakingSpace = messageFormatter.getNonBreakingSpace();
        String jobLine = String.format("Job%sname:%s%s", nonBreakingSpace, nonBreakingSpace, jobName);
        String formattedJob = messageFormatter.emphasize(jobLine);
        appendSection(chunkedStringBuilder, formattedJob);
        appendSection(chunkedStringBuilder, simpleMessage.getSummary());
        appendSection(chunkedStringBuilder, simpleMessage.getDescription());

        appendLinkableItem(chunkedStringBuilder, simpleMessage.getProvider(), false);

        for (LinkableItem detail : simpleMessage.getDetails()) {
            appendLinkableItem(chunkedStringBuilder, detail, false);
        }

        return chunkedStringBuilder.collectCurrentChunks();
    }

    private void appendSection(ChunkedStringBuilder chunkedStringBuilder, String txt) {
        String encodedTxt = messageFormatter.encode(txt);
        chunkedStringBuilder.append(encodedTxt);
        chunkedStringBuilder.append(messageFormatter.getLineSeparator());
        chunkedStringBuilder.append(messageFormatter.getSectionSeparator());
        chunkedStringBuilder.append(messageFormatter.getLineSeparator());
    }

    private void appendLinkableItem(ChunkedStringBuilder chunkedStringBuilder, LinkableItem linkableItem, boolean bold) {
        String detailString = linkableItemConverter.convertToString(linkableItem, bold);
        chunkedStringBuilder.append(detailString);
        chunkedStringBuilder.append(messageFormatter.getLineSeparator());
    }

}
