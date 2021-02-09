/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopys.integration.alert.channel.api.convert;

import java.util.Optional;

import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class LinkableItemConverter {
    private final ChannelMessageFormatter formatter;

    public LinkableItemConverter(ChannelMessageFormatter formatter) {
        this.formatter = formatter;
    }

    public String convertToString(LinkableItem linkableItem, boolean bold) {
        String name = formatter.encode(linkableItem.getLabel());
        String value = formatter.encode(linkableItem.getValue());
        Optional<String> optionalUrl = linkableItem.getUrl();

        if (bold) {
            name = formatter.emphasize(name);
            value = formatter.emphasize(value);
        }

        if (optionalUrl.isPresent()) {
            // The nuance around stylizing links adds too much complexity for too little value to worry about emphasizing them.
            value = createLinkableItemValueString(linkableItem);
        }

        return String.format("%s:%s%s", name, formatter.getNonBreakingSpace(), value);
    }

    private String createLinkableItemValueString(LinkableItem linkableItem) {
        String value = formatter.encode(linkableItem.getValue());
        Optional<String> optionalUrl = linkableItem.getUrl();

        String formattedString = value;
        if (optionalUrl.isPresent()) {
            String urlString = formatter.encode(optionalUrl.get());
            formattedString = formatter.createLink(value, urlString);
        }
        return formattedString;
    }

}
