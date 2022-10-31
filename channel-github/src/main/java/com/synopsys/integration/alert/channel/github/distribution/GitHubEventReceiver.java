package com.synopsys.integration.alert.channel.github.distribution;

import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.DistributionEventReceiver;
import com.synopsys.integration.alert.common.persistence.model.job.details.GitHubJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class GitHubEventReceiver extends DistributionEventReceiver<GitHubJobDetailsModel> {
    public GitHubEventReceiver(
        final Gson gson,
        final TaskExecutor taskExecutor,
        final GitHubEventHandler distributionEventHandler
    ) {
        super(gson, taskExecutor, ChannelKeys.GITHUB, distributionEventHandler);
    }
}
