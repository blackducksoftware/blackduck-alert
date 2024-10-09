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
