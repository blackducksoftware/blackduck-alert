/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.jira.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.model.IssueContentModel;
import com.synopsys.integration.alert.common.channel.ChannelMessageParser;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

@Component
public class JiraDescriptionMessageParser extends ChannelMessageParser {
    public static final int TITLE_LIMIT = 255;
    private static final int TEXT_LIMIT = 30000;
    private static final String LINE_SEPARATOR = "\n";

    public String createTitle(String provider, LinkableItem topic, LinkableItem subTopic, ComponentItem arbitraryItem) {
        StringBuilder title = new StringBuilder();
        title.append("Alert - Provider: ");
        title.append(provider);
        title.append(createTitlePartStringPrefixedWithComma(topic));

        if (null != subTopic) {
            title.append(createTitlePartStringPrefixedWithComma(subTopic));
        }

        title.append(createTitlePartStringPrefixedWithComma(arbitraryItem.getComponent()));
        arbitraryItem
            .getSubComponent()
            .ifPresent(linkableItem -> title.append(createTitlePartStringPrefixedWithComma(linkableItem)));

        if (arbitraryItem.collapseOnCategory()) {
            title.append(", ");
            title.append(arbitraryItem.getCategory());
        } else {
            title.append(createTitlePartStringPrefixedWithComma(arbitraryItem.getCategoryItem()));
        }

        return StringUtils.abbreviate(title.toString(), TITLE_LIMIT);
    }

    public IssueContentModel createDescription(LinkableItem commonTopic, LinkableItem subTopic, Collection<ComponentItem> componentItems, String providerName, ComponentItem arbitraryItem) {
        String title = createTitle(providerName, commonTopic, subTopic, arbitraryItem);

        StringBuilder description = new StringBuilder();
        description.append("Provider: ");
        description.append(providerName);
        description.append(LINE_SEPARATOR);
        description.append(commonTopic.getName());
        description.append(": ");
        description.append(commonTopic.getValue());
        description.append(LINE_SEPARATOR);
        if (null != subTopic) {
            String valueString = createLinkableItemValueString(subTopic);
            description.append(subTopic);
            description.append(": ");
            description.append(valueString);
            description.append(LINE_SEPARATOR);
        }

        List<String> additionalComments = new ArrayList<>();
        List<String> descriptionAttributes = new ArrayList<>();
        String componentSection = createCommonComponentItemString(arbitraryItem);
        description.append(componentSection);

        // FIXME splitAdditionalComponentInfoForDescription(description.length(), componentItems, descriptionAttributes, additionalComments);
        description.append(StringUtils.join(descriptionAttributes, LINE_SEPARATOR));
        return IssueContentModel.of(title, description.toString(), additionalComments);
    }

    @Override
    protected String encodeString(String txt) {
        return txt;
    }

    @Override
    protected String emphasize(String txt) {
        // TODO emphasize?
        return txt;
    }

    @Override
    protected String createLink(String txt, String url) {
        return String.format("[%s|%s]", txt, url);
    }

    @Override
    protected String getLineSeparator() {
        return "\n";
    }

    private String createTitlePartStringPrefixedWithComma(LinkableItem linkableItem) {
        return String.format(", %s: %s", linkableItem.getName(), linkableItem.getValue());
    }

}
