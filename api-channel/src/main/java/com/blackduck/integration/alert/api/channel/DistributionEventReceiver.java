/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel;

import org.springframework.core.task.TaskExecutor;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKey;
import com.blackduck.integration.alert.api.event.AlertMessageListener;
import com.blackduck.integration.alert.api.processor.distribute.DistributionEvent;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.google.gson.Gson;

public abstract class DistributionEventReceiver<D extends DistributionJobDetailsModel> extends AlertMessageListener<DistributionEvent> {
    protected DistributionEventReceiver(Gson gson, TaskExecutor taskExecutor, ChannelKey channelKey, DistributionEventHandler<D> distributionEventHandler) {
        super(gson, taskExecutor, channelKey.getUniversalKey(), DistributionEvent.class, distributionEventHandler);
    }

}
