package com.blackduck.integration.alert.channel.msteams.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.DistributionEventReceiver;
import com.blackduck.integration.alert.api.descriptor.MsTeamsKey;
import com.blackduck.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.google.gson.Gson;

@Component
public class MSTeamsDistributionEventReceiver extends DistributionEventReceiver<MSTeamsJobDetailsModel> {
    @Autowired
    public MSTeamsDistributionEventReceiver(Gson gson, TaskExecutor taskExecutor, MsTeamsKey channelKey, MSTeamsDistributionEventHandler distributionEventHandler) {
        super(gson, taskExecutor, channelKey, distributionEventHandler);
    }

}
