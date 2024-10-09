package com.blackduck.integration.alert.channel.slack.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.DistributionEventHandler;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.common.persistence.accessor.SlackJobDetailsAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;

@Component
public class SlackDistributionEventHandler extends DistributionEventHandler<SlackJobDetailsModel> {
    @Autowired
    public SlackDistributionEventHandler(SlackChannel channel, SlackJobDetailsAccessor jobDetailsAccessor, EventManager eventManager) {
        super(channel, jobDetailsAccessor, eventManager);
    }

}
