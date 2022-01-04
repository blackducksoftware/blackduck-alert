/*
 * api-channel
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.convert;

import org.jetbrains.annotations.Nullable;

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

    private String convertToString(String encodedLabel, String encodedValue, @Nullable String encodedUrl, boolean bold) {
        String label = encodedLabel;
        String value = encodedValue;
        if (bold) {
            label = formatter.emphasize(encodedLabel);
            value = formatter.emphasize(encodedValue);
        }

        if (null != encodedUrl) {
            // The nuance around stylizing links adds too much complexity for too little value to worry about emphasizing them.
            value = formatter.createLink(encodedValue, encodedUrl);
        }

        return String.format("%s:%s%s", label, formatter.getNonBreakingSpace(), value);
    }

}
