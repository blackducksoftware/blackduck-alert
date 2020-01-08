/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.msteams;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.channel.message.ChannelMessageParser;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;

@Component
public class MsTeamsMessageParser extends ChannelMessageParser {
    private static final Map<Character, String> reservedChars = Map.of(
        '*', "\\*",
        '~', "\\~",
        '#', "\\#",
        '-', "\\-",
        '_', "\\_"
    );

    private MarkupEncoderUtil markupEncoderUtil;

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
            LinkableItem newItem = new LinkableItem(linkableItem.getName(), linkableItem.getValue(), encodedUrl);
            return super.createLinkableItemValueString(newItem);
        }

        return super.createLinkableItemValueString(linkableItem);
    }

    public MsTeamsMessage createMsTeamsMessage(MessageContentGroup messageContentGroup) {
        String header = createHeader(messageContentGroup);
        String commonTopic = getCommonTopic(messageContentGroup);
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
        String componentSubTopic = getComponentSubTopic(providerMessageContent);
        msTeamsSection.setSubTopic(componentSubTopic);
        List<String> componentItemMessage = createComponentItemMessage(providerMessageContent);
        msTeamsSection.setComponentsMessage(componentItemMessage);

        return msTeamsSection;
    }
}
