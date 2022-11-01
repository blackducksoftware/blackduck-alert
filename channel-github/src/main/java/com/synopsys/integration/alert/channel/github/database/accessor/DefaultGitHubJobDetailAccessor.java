package com.synopsys.integration.alert.channel.github.database.accessor;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.accessor.GitHubJobDetailAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.GitHubJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class DefaultGitHubJobDetailAccessor implements GitHubJobDetailAccessor {

    @Override
    public Optional<GitHubJobDetailsModel> retrieveDetails(final UUID jobId) {
        return Optional.empty();
    }

    @Override
    public DescriptorKey getDescriptorKey() {
        return null;
    }

    @Override
    public GitHubJobDetailsModel saveJobDetails(final UUID jobId, final DistributionJobDetailsModel jobDetailsModel) {
        return null;
    }

    @Override
    public GitHubJobDetailsModel saveConcreteJobDetails(final UUID jobId, final GitHubJobDetailsModel jobDetails) {
        return null;
    }
}
