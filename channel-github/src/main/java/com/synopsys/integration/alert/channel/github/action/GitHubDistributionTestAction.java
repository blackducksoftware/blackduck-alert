package com.synopsys.integration.alert.channel.github.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.action.DistributionChannelMessageTestAction;
import com.synopsys.integration.alert.channel.github.distribution.GitHubChannel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.GitHubJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.GitHubChannelKey;

@Component
public class GitHubDistributionTestAction extends DistributionChannelMessageTestAction<GitHubJobDetailsModel> {

    @Autowired
    protected GitHubDistributionTestAction(
        final GitHubChannelKey gitHubChannelKey,
        final GitHubChannel gitHubChannel
    ) {
        super(gitHubChannelKey, gitHubChannel);
    }

    @Override
    protected GitHubJobDetailsModel resolveTestDistributionDetails(final DistributionJobModel testJobModel) {
        return testJobModel.getDistributionJobDetails().getAs(DistributionJobDetailsModel.GITHUB);
    }
}
