/*
 * blackduck-alert
 *
 * Copyright (c) 2025 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.convert;

import com.blackduck.integration.alert.api.channel.convert.ChannelMessageFormatter;
import com.blackduck.integration.alert.api.processor.extract.model.SimpleMessage;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

public class JiraCloudSimpleMessageConverter {
    private final ChannelMessageFormatter messageFormatter;

    public JiraCloudSimpleMessageConverter(ChannelMessageFormatter messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    public void convertToFormattedMessageChunks(SimpleMessage simpleMessage, String jobName, AtlassianDocumentBuilder documentBuilder) {

        String nonBreakingSpace = messageFormatter.getNonBreakingSpace();
        String jobLine = String.format("Job%sname:%s%s", nonBreakingSpace, nonBreakingSpace, jobName);
        String formattedJob = messageFormatter.emphasize(jobLine);
        appendSection(formattedJob, documentBuilder);
        appendSection(simpleMessage.getSummary(), documentBuilder);
        appendSection(simpleMessage.getDescription(), documentBuilder);

        appendLinkableItem(documentBuilder, simpleMessage.getProvider(), false);

        for (LinkableItem detail : simpleMessage.getDetails()) {
            appendLinkableItem(documentBuilder, detail, false);
        }
    }

    private void appendSection(String txt, AtlassianDocumentBuilder documentBuilder) {
        String encodedTxt = messageFormatter.encode(txt);
        documentBuilder.addTextNode(encodedTxt)
            .addTextNode(messageFormatter.getLineSeparator())
            .addTextNode(messageFormatter.getSectionSeparator())
            .addTextNode(messageFormatter.getLineSeparator())
            .addParagraphNode();
    }

    private void appendLinkableItem(AtlassianDocumentBuilder documentBuilder, LinkableItem linkableItem, boolean bold) {
        documentBuilder.addTextNode(linkableItem, bold)
            .addTextNode(messageFormatter.getLineSeparator());
    }
}
