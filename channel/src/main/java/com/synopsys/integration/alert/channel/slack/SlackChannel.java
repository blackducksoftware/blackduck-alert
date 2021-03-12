/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.slack;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.slack.parser.SlackChannelEventParser;
import com.synopsys.integration.alert.channel.util.RestChannelUtility;
import com.synopsys.integration.alert.common.channel.NamedDistributionChannel;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;

@Component
public class SlackChannel extends NamedDistributionChannel {
    private final RestChannelUtility restChannelUtility;
    private final SlackChannelEventParser slackChannelEventParser;

    @Autowired
    public SlackChannel(Gson gson, AuditAccessor auditAccessor, RestChannelUtility restChannelUtility, SlackChannelEventParser slackChannelEventParser) {
        super(ChannelKeys.SLACK, gson, auditAccessor);
        this.restChannelUtility = restChannelUtility;
        this.slackChannelEventParser = slackChannelEventParser;
    }

    @Override
    public void distributeMessage(DistributionEvent event) throws IntegrationException {
        List<Request> requests = slackChannelEventParser.createRequests(event);
        restChannelUtility.sendMessage(requests, event.getDestination());
    }

    public List<Request> createRequests(DistributionEvent event) throws IntegrationException {
        return slackChannelEventParser.createRequests(event);
    }

}
