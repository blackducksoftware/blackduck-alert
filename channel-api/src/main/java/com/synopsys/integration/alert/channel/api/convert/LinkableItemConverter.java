/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.convert;

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
