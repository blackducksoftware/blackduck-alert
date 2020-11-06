/**
 * channel
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.msteams;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.msteams.descriptor.MsTeamsDescriptor;
import com.synopsys.integration.alert.channel.util.RestChannelUtility;
import com.synopsys.integration.alert.common.channel.AutoActionable;
import com.synopsys.integration.alert.common.channel.NamedDistributionChannel;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.descriptor.api.MsTeamsKey;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;

@Component
public class MsTeamsChannel extends NamedDistributionChannel implements AutoActionable {
    private final RestChannelUtility restChannelUtility;
    private final MsTeamsEventParser msTeamsEventParser;
    private final MsTeamsMessageParser msTeamsMessageParser;

    @Autowired
    public MsTeamsChannel(MsTeamsKey msTeamsKey, Gson gson, AuditAccessor auditAccessor, RestChannelUtility restChannelUtility, MsTeamsEventParser msTeamsEventParser,
        MsTeamsMessageParser msTeamsMessageParser) {
        super(msTeamsKey, gson, auditAccessor);
        this.restChannelUtility = restChannelUtility;
        this.msTeamsEventParser = msTeamsEventParser;
        this.msTeamsMessageParser = msTeamsMessageParser;
    }

    @Override
    public void distributeMessage(DistributionEvent event) throws IntegrationException {
        FieldUtility fields = event.getFieldUtility();
        String webhook = fields.getString(MsTeamsDescriptor.KEY_WEBHOOK)
                             .orElseThrow(() -> AlertFieldException.singleFieldError(MsTeamsDescriptor.KEY_WEBHOOK, "MS Teams missing the required webhook field - the distribution configuration is likely invalid."));

        MsTeamsMessage msTeamsMessage = msTeamsMessageParser.createMsTeamsMessage(event.getContent());
        List<Request> teamsRequests = new LinkedList<>();
        for (MsTeamsMessage message : msTeamsEventParser.splitMessages(msTeamsMessage)) {
            String json = msTeamsEventParser.toJson(message);
            Request request = restChannelUtility.createPostMessageRequest(webhook, new HashMap<>(), json);
            teamsRequests.add(request);
        }

        restChannelUtility.sendMessage(teamsRequests, event.getDestination());
    }

}
