/**
 * channel
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
