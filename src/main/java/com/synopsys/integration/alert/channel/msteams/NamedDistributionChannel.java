package com.synopsys.integration.alert.channel.msteams;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.channel.DistributionChannel;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.persistence.accessor.AuditUtility;
import com.synopsys.integration.exception.IntegrationException;

public abstract class NamedDistributionChannel extends DistributionChannel {
    private String channelName;

    public NamedDistributionChannel(String channelName, Gson gson, AuditUtility auditUtility) {
        super(gson, auditUtility);
        this.channelName = channelName;
    }

    @Override
    public String sendMessage(final DistributionEvent event) throws IntegrationException {
        distributeMessage(event);
        return String.format("Successfully sent %s message.", channelName);
    }

    @Override
    public String getDestinationName() {
        return channelName;
    }

    public abstract void distributeMessage(DistributionEvent event) throws IntegrationException;

}
