/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.msteams;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.channel.message.ChannelMessageParser;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;

@Component
@Deprecated
public class MsTeamsMessageParser extends ChannelMessageParser {
    private static final Map<Character, String> reservedChars = Map.of(
        '*', "\\*",
        '~', "\\~",
        '#', "\\#",
        '-', "\\-",
        '_', "\\_"
    );

    private final MarkupEncoderUtil markupEncoderUtil;

    @Autowired
    public MsTeamsMessageParser(MarkupEncoderUtil markupEncoderUtil) {
        this.markupEncoderUtil = markupEncoderUtil;
    }

    @Override
    protected String encodeString(String txt) {
        return markupEncoderUtil.encodeMarkup(reservedChars, txt);
    }

    @Override
    protected String emphasize(String txt) {
        return String.format("**%s**", txt);
    }

    @Override
    protected String createLink(String txt, String url) {
        return String.format("[%s](%s)", txt, url);
    }

    @Override
    protected String getLineSeparator() {
        return "\r\n\r\n";
    }

    @Override
    protected String createMessageSeparator(String title) {
        return title;
    }

    @Override
    public String createHeader(MessageContentGroup messageContentGroup) {
        return String.format("Received a message from %s", messageContentGroup.getCommonProvider().getValue());
    }

    @Override
    protected String createLinkableItemValueString(LinkableItem linkableItem) {
        String itemUrl = linkableItem.getUrl().orElse("");
        if (StringUtils.isNotBlank(itemUrl) && itemUrl.contains(" ")) {
            String encodedUrl = itemUrl.replace(" ", "%20");
            LinkableItem newItem = new LinkableItem(linkableItem.getLabel(), linkableItem.getValue(), encodedUrl);
            return super.createLinkableItemValueString(newItem);
        }

        return super.createLinkableItemValueString(linkableItem);
    }

    public MsTeamsMessage createMsTeamsMessage(MessageContentGroup messageContentGroup) {
        String header = createHeader(messageContentGroup);
        ItemOperation nullableTopLevelAction = getNullableTopLevelAction(messageContentGroup).orElse(null);
        String commonTopic = getCommonTopic(messageContentGroup, nullableTopLevelAction);
        List<MsTeamsSection> messagePieces = createMessageParts(messageContentGroup);
        return new MsTeamsMessage(header, commonTopic, messagePieces);
    }

    private List<MsTeamsSection> createMessageParts(MessageContentGroup messageContentGroup) {
        return messageContentGroup.getSubContent()
                   .stream()
                   .map(this::createMessageSection)
                   .collect(Collectors.toList());
    }

    private MsTeamsSection createMessageSection(ProviderMessageContent providerMessageContent) {
        MsTeamsSection msTeamsSection = new MsTeamsSection();
        ItemOperation nullableTopLevelAction = providerMessageContent.isTopLevelActionOnly() ? providerMessageContent.getAction().orElse(null) : null;
        String componentSubTopic = getComponentSubTopic(providerMessageContent, nullableTopLevelAction);
        msTeamsSection.setSubTopic(componentSubTopic);
        List<String> componentItemMessage = createComponentItemMessage(providerMessageContent);
        msTeamsSection.setComponentsMessage(componentItemMessage);

        return msTeamsSection;
    }

}
