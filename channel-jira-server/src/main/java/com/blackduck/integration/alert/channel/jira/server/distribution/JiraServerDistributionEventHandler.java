package com.blackduck.integration.alert.channel.jira.server.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.DistributionEventHandler;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.common.persistence.accessor.JiraServerJobDetailsAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;

@Component
public class JiraServerDistributionEventHandler extends DistributionEventHandler<JiraServerJobDetailsModel> {
    @Autowired
    public JiraServerDistributionEventHandler(
        JiraServerChannel channel,
        JiraServerJobDetailsAccessor jobDetailsAccessor,
        EventManager eventManager
    ) {
        super(channel, jobDetailsAccessor, eventManager);
    }

}
