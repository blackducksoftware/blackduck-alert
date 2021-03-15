/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel;

import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.exception.IntegrationException;

@Deprecated
public abstract class NamedDistributionChannel extends DistributionChannel {
    private final ChannelKey channelKey;

    public NamedDistributionChannel(ChannelKey channelKey) {
        super();
        this.channelKey = channelKey;
    }

    @Override
    public MessageResult sendMessage(DistributionEvent event) throws IntegrationException {
        distributeMessage(event);
        String statusMessage = String.format("Successfully sent %s message.", channelKey.getDisplayName());
        return new MessageResult(statusMessage);
    }

    public abstract void distributeMessage(DistributionEvent event) throws IntegrationException;

}
