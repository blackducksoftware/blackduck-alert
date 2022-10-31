package com.synopsys.integration.alert.common.persistence.model.job.details;

import java.util.UUID;

import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

public class GitHubJobDetailsModel extends DistributionJobDetailsModel {
    
    public GitHubJobDetailsModel(final UUID jobId) {
        super(ChannelKeys.GITHUB, jobId);
    }
}
