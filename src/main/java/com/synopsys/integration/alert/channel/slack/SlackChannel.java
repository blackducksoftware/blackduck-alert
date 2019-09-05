/**
 * blackduck-alert
 * <p>
 * Copyright (c) 2019 Synopsys, Inc.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.channel.slack;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.msteams.SimpleChannelActions;
import com.synopsys.integration.alert.channel.util.RestChannelUtility;
import com.synopsys.integration.alert.common.channel.NamedDistributionChannel;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.persistence.accessor.AuditUtility;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component(value = SlackChannel.COMPONENT_NAME)
public class SlackChannel extends NamedDistributionChannel implements SimpleChannelActions {
    public static final String COMPONENT_NAME = "channel_slack";

    private final RestChannelUtility restChannelUtility;
    private final SlackChannelEventParser slackChannelEventParser;

    @Autowired
    public SlackChannel(SlackChannelKey slackChannelKey, Gson gson, AuditUtility auditUtility, RestChannelUtility restChannelUtility, SlackChannelEventParser slackChannelEventParser) {
        super(slackChannelKey, gson, auditUtility);
        this.restChannelUtility = restChannelUtility;
        this.slackChannelEventParser = slackChannelEventParser;
    }

    @Override
    public void distributeMessage(final DistributionEvent event) throws IntegrationException {
        final List<Request> requests = slackChannelEventParser.createRequests(event);
        restChannelUtility.sendMessage(requests, event.getDestination());
    }

    public List<Request> createRequests(DistributionEvent event) throws IntegrationException {
        return slackChannelEventParser.createRequests(event);
    }

}
