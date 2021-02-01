/*
 * processor-api
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
package com.synopsys.integration.alert.processor.api.distribute;

import java.util.UUID;

import com.synopsys.integration.alert.common.event.AlertEvent;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;

public class DistributionEventV2 extends AlertEvent {
    private final UUID jobId;
    private final Long auditId;

    private final ProviderMessageHolder providerMessages;

    public DistributionEventV2(ChannelKey destination, UUID jobId, Long auditId, ProviderMessageHolder providerMessages) {
        super(destination.getUniversalKey());
        this.jobId = jobId;
        this.auditId = auditId;
        this.providerMessages = providerMessages;
    }

    public UUID getJobId() {
        return jobId;
    }

    public Long getAuditId() {
        return auditId;
    }

    public ProviderMessageHolder getProviderMessages() {
        return providerMessages;
    }

}
