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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.workflow.MessageReceiver;
import com.synopsys.integration.alert.processor.api.distribute.DistributionEventV2;

public abstract class DistributionEventReceiver<D extends DistributionJobDetailsModel> extends MessageReceiver<DistributionEventV2> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AuditAccessor auditAccessor;
    private final JobDetailsAccessor<D> jobDetailsAccessor;
    private final DistributionChannelV2<D> channel;

    protected DistributionEventReceiver(Gson gson, AuditAccessor auditAccessor, JobDetailsAccessor<D> jobDetailsAccessor, DistributionChannelV2<D> channel) {
        super(gson, DistributionEventV2.class);
        this.auditAccessor = auditAccessor;
        this.jobDetailsAccessor = jobDetailsAccessor;
        this.channel = channel;
    }

    @Override
    public final void handleEvent(DistributionEventV2 event) {
        Optional<D> details = jobDetailsAccessor.retrieveDetails(event.getJobId());
        if (details.isPresent()) {
            try {
                channel.distributeMessages(details.get(), event.getProviderMessages());
                auditAccessor.setAuditEntrySuccess(Set.of(event.getAuditId()));
            } catch (AlertException e) {
                handleException(e, event);
            }
        } else {
            handleJobDetailsMissing(event);
        }
    }

    protected void handleException(AlertException e, DistributionEventV2 event) {
        logger.error("An exception occurred while handling the following event: {}", event, e);
        auditAccessor.setAuditEntryFailure(Set.of(event.getAuditId()), "An exception occurred during message distribution", e);
    }

    protected void handleJobDetailsMissing(DistributionEventV2 event) {
        String failureMessage = "Received a distribution event for a Job that no longer exists";
        logger.warn("{}. Destination: {}", failureMessage, event.getDestination());
        auditAccessor.setAuditEntryFailure(Set.of(event.getAuditId()), failureMessage, null);
    }

}
