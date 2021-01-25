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

import java.util.Optional;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.workflow.MessageReceiver;
import com.synopsys.integration.alert.processor.api.distribute.DistributionEventV2;

public abstract class DistributionEventReceiver<D extends DistributionJobDetailsModel> extends MessageReceiver<DistributionEventV2> {
    private final JobDetailsAccessor<D> jobDetailsAccessor;
    private final DistributionChannelV2<D> channel;

    public DistributionEventReceiver(Gson gson, JobDetailsAccessor<D> jobDetailsAccessor, DistributionChannelV2<D> channel) {
        super(gson, DistributionEventV2.class);
        this.jobDetailsAccessor = jobDetailsAccessor;
        this.channel = channel;
    }

    @Override
    public final void handleEvent(DistributionEventV2 event) {
        Optional<D> details = jobDetailsAccessor.retrieveDetails(event.getJobId());
        if (details.isPresent()) {
            channel.distributeMessages(details.get(), event.getProviderMessages());
        } else {
            handleJobDetailsMissing(event);
        }
    }

    protected void handleJobDetailsMissing(DistributionEventV2 event) {
        // FIXME implement
        //  log
        //  update audit status
    }

}
