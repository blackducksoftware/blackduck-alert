/**
 * channel-api
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
package com.synopys.integration.alert.channel.api;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.workflow.MessageReceiver;
import com.synopsys.integration.alert.processor.api.distribute.DistributionEventV2;

public abstract class ChannelReceiver<D extends DistributionJobDetailsModel, T> extends MessageReceiver<DistributionEventV2> {
    private final DistributionChannelV2<D, T> channel;
    private final JobDetailsAccessor<D> jobDetailsAccessor;

    public ChannelReceiver(Gson gson, DistributionChannelV2<D, T> channel, JobDetailsAccessor<D> jobDetailsAccessor) {
        super(gson, DistributionEventV2.class);
        this.channel = channel;
        this.jobDetailsAccessor = jobDetailsAccessor;
    }

    @Override
    public final void handleEvent(DistributionEventV2 event) {
        D details = jobDetailsAccessor.retrieveDetails(event.getJobId());
        channel.processAndSend(details, event.getProviderMessages());
    }

}
