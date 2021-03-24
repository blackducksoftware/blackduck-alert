/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.convert;

import java.util.List;

import com.synopsys.integration.alert.common.channel.message.ChunkedStringBuilder;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;

public class SimpleMessageConverter extends ProviderMessageConverter<SimpleMessage> {
    private final ChannelMessageFormatter messageFormatter;

    public SimpleMessageConverter(ChannelMessageFormatter messageFormatter) {
        super(messageFormatter);
        this.messageFormatter = messageFormatter;
    }

    @Override
    public List<String> convertToFormattedMessageChunks(SimpleMessage simpleMessage) {
        ChunkedStringBuilder chunkedStringBuilder = new ChunkedStringBuilder(messageFormatter.getMaxMessageLength());

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
        String detailString = createLinkableItemString(linkableItem, bold);
        chunkedStringBuilder.append(detailString);
        chunkedStringBuilder.append(messageFormatter.getLineSeparator());
    }

}
