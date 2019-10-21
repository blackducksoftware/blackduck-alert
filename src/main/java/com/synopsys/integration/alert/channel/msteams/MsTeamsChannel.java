/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.msteams.descriptor.MsTeamsDescriptor;
import com.synopsys.integration.alert.channel.util.RestChannelUtility;
import com.synopsys.integration.alert.common.channel.AutoActionable;
import com.synopsys.integration.alert.common.channel.NamedDistributionChannel;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditUtility;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;

@Component
public class MsTeamsChannel extends NamedDistributionChannel implements AutoActionable {
    private RestChannelUtility restChannelUtility;
    private MsTeamsEventParser msTeamsEventParser;
    private MsTeamsMessageParser msTeamsMessageParser;

    @Autowired
    public MsTeamsChannel(MsTeamsKey msTeamsKey, Gson gson, AuditUtility auditUtility, RestChannelUtility restChannelUtility, MsTeamsEventParser msTeamsEventParser,
        MsTeamsMessageParser msTeamsMessageParser) {
        super(msTeamsKey, gson, auditUtility);
        this.restChannelUtility = restChannelUtility;
        this.msTeamsEventParser = msTeamsEventParser;
        this.msTeamsMessageParser = msTeamsMessageParser;
    }

    @Override
    public void distributeMessage(DistributionEvent event) throws IntegrationException {
        FieldAccessor fields = event.getFieldAccessor();
        String webhook = fields.getString(MsTeamsDescriptor.KEY_WEBHOOK)
                             .orElseThrow(() -> AlertFieldException.singleFieldError(MsTeamsDescriptor.KEY_WEBHOOK, "MS Teams missing the required webhook field - the distribution configuration is likely invalid."));

        String messagePieces = msTeamsMessageParser.createMessage(event.getContent());
        //        MsTeamsMessage msTeamsMessage = msTeamsEventParser.createMessage(event);
        //        String json = msTeamsEventParser.toJson(msTeamsMessage);

        Request request = restChannelUtility.createPostMessageRequest(webhook, new HashMap<>(), messagePieces);

        restChannelUtility.sendSingleMessage(request, event.getDestination());
    }

}
