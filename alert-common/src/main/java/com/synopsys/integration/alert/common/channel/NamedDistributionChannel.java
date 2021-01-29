/*
 * alert-common
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
package com.synopsys.integration.alert.common.channel;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.exception.IntegrationException;

public abstract class NamedDistributionChannel extends DistributionChannel {
    private final ChannelKey channelKey;

    public NamedDistributionChannel(ChannelKey channelKey, Gson gson, AuditAccessor auditAccessor) {
        super(gson, auditAccessor);
        this.channelKey = channelKey;
    }

    @Override
    public MessageResult sendMessage(DistributionEvent event) throws IntegrationException {
        distributeMessage(event);
        String statusMessage = String.format("Successfully sent %s message.", channelKey.getDisplayName());
        return new MessageResult(statusMessage);
    }

    public abstract void distributeMessage(DistributionEvent event) throws IntegrationException;

    @Override
    public String getDestinationName() {
        return channelKey.getUniversalKey();
    }

    public ChannelKey getChannelKey() {
        return channelKey;
    }

    public DistributionChannel getChannel() {
        return this;
    }

}
