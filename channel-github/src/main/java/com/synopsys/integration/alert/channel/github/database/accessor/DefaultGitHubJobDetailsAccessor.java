package com.synopsys.integration.alert.channel.github.database.accessor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.channel.github.database.job.GitHubJobDetailsEntity;
import com.synopsys.integration.alert.channel.github.database.job.GitHubJobDetailsRepository;
import com.synopsys.integration.alert.common.persistence.accessor.GitHubJobDetailAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.GitHubJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.GitHubChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class DefaultGitHubJobDetailsAccessor implements GitHubJobDetailAccessor {
    private final GitHubChannelKey channelKey;
    private final GitHubJobDetailsRepository gitHubJobDetailsRepository;

    @Autowired
    public DefaultGitHubJobDetailsAccessor(GitHubChannelKey channelKey, GitHubJobDetailsRepository gitHubJobDetailsRepository) {
        this.channelKey = channelKey;
        this.gitHubJobDetailsRepository = gitHubJobDetailsRepository;
    }

    @Override
    public DescriptorKey getDescriptorKey() {
        return channelKey;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GitHubJobDetailsModel> retrieveDetails(UUID jobId) {
        return Optional.empty();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public GitHubJobDetailsModel saveJobDetails(UUID jobId, DistributionJobDetailsModel jobDetailsModel) {
        GitHubJobDetailsModel gitHubJobDetailsModel = jobDetailsModel.getAs(GitHubJobDetailsModel.class);
        return saveConcreteJobDetails(jobId, gitHubJobDetailsModel);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public GitHubJobDetailsModel saveConcreteJobDetails(UUID jobId, GitHubJobDetailsModel jobDetails) {
        GitHubJobDetailsEntity gitHubJobDetailsToSave = new GitHubJobDetailsEntity(jobId, jobDetails.getRepositoryUrl(), jobDetails.getPullRequestTitlePrefix());
        GitHubJobDetailsEntity savedJobDetails = gitHubJobDetailsRepository.save(gitHubJobDetailsToSave);
        return convertToModel(savedJobDetails);
    }

    private GitHubJobDetailsModel convertToModel(GitHubJobDetailsEntity jobDetails) {
        return new GitHubJobDetailsModel(jobDetails.getJobId(), jobDetails.getRepositoryUrl(), jobDetails.getPullRequestTitlePrefix());
    }
}
