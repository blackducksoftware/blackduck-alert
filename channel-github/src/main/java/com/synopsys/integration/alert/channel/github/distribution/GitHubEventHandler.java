package com.synopsys.integration.alert.channel.github.distribution;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.DistributionChannel;
import com.synopsys.integration.alert.api.channel.DistributionEventHandler;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.persistence.accessor.GitHubJobDetailAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.GitHubJobDetailsModel;

@Component
public class GitHubEventHandler extends DistributionEventHandler<GitHubJobDetailsModel> {
    public GitHubEventHandler(
        DistributionChannel<GitHubJobDetailsModel> channel,
        GitHubJobDetailAccessor jobDetailsAccessor,
        EventManager eventManager
    ) {
        super(channel, jobDetailsAccessor, eventManager);
    }
}
