/*
 * api-channel
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel;

import org.springframework.core.task.TaskExecutor;

import com.google.gson.Gson;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKey;
import com.blackduck.integration.alert.api.event.AlertMessageListener;
import com.blackduck.integration.alert.api.processor.distribute.DistributionEvent;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public abstract class DistributionEventReceiver<D extends DistributionJobDetailsModel> extends AlertMessageListener<DistributionEvent> {
    protected DistributionEventReceiver(Gson gson, TaskExecutor taskExecutor, ChannelKey channelKey, DistributionEventHandler<D> distributionEventHandler) {
        super(gson, taskExecutor, channelKey.getUniversalKey(), DistributionEvent.class, distributionEventHandler);
    }

}
