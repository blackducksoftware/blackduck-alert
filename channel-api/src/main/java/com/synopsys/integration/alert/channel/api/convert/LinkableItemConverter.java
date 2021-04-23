/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.convert;

import javax.annotation.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class LinkableItemConverter {
    private final ChannelMessageFormatter formatter;

    public LinkableItemConverter(ChannelMessageFormatter formatter) {
        this.formatter = formatter;
    }

    public String convertToString(LinkableItem linkableItem, boolean bold) {
        return convertToString(
            formatter.encode(linkableItem.getLabel()),
            formatter.encode(linkableItem.getValue()),
            linkableItem.getUrl().map(formatter::encode).orElse(null),
            bold
        );
    }

    public String convertToStringWithoutLink(LinkableItem linkableItem, boolean bold) {
        return convertToString(
            formatter.encode(linkableItem.getLabel()),
            formatter.encode(linkableItem.getValue()),
            null,
            bold
        );
    }

    private String convertToString(String encodedName, String encodedValue, @Nullable String encodedUrl, boolean bold) {
        String name = encodedName;
        String value = encodedValue;
        if (bold) {
            name = formatter.emphasize(encodedName);
            value = formatter.emphasize(encodedValue);
        }

        if (null != encodedUrl) {
            // The nuance around stylizing links adds too much complexity for too little value to worry about emphasizing them.
            value = formatter.createLink(encodedValue, encodedUrl);
        }

        return String.format("%s:%s%s", name, formatter.getNonBreakingSpace(), value);
    }

}
