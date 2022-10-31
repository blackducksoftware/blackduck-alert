package com.synopsys.integration.alert.channel.github.database.accessor;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.accessor.GitHubJobDetailAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.GitHubJobDetailsModel;

@Component
public class DefaultGitHubJobDetailAccessor implements GitHubJobDetailAccessor {

    @Override
    public Optional<GitHubJobDetailsModel> retrieveDetails(final UUID jobId) {
        return Optional.empty();
    }
}
