package com.blackduck.integration.alert.channel.msteams.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.DistributionEventHandler;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.common.persistence.accessor.MSTeamsJobDetailsAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;

@Component
public class MSTeamsDistributionEventHandler extends DistributionEventHandler<MSTeamsJobDetailsModel> {
    @Autowired
    public MSTeamsDistributionEventHandler(MSTeamsChannel channel, MSTeamsJobDetailsAccessor jobDetailsAccessor, EventManager eventManager) {
        super(channel, jobDetailsAccessor, eventManager);
    }

}
