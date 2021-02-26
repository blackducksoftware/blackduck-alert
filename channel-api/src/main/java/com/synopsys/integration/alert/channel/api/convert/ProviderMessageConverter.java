/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.convert;

import java.util.List;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessage;

public abstract class ProviderMessageConverter<T extends ProviderMessage<T>> {
    private final LinkableItemConverter linkableItemConverter;

    public ProviderMessageConverter(ChannelMessageFormatter formatter) {
        linkableItemConverter = new LinkableItemConverter(formatter);
    }

    public abstract List<String> convertToFormattedMessageChunks(T message);

    protected String createLinkableItemString(LinkableItem linkableItem, boolean bold) {
        return linkableItemConverter.convertToString(linkableItem, bold);
    }

}
