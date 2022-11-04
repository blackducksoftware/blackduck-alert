package com.synopsys.integration.alert.channel.github.action;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.github.descriptor.GitHubDescriptor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.GitHubJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.processor.DistributionJobDetailsExtractor;
import com.synopsys.integration.alert.common.persistence.model.job.details.processor.DistributionJobFieldExtractor;
import com.synopsys.integration.alert.descriptor.api.GitHubChannelKey;

@Component
public class GitHubJobDetailsExtractor extends DistributionJobDetailsExtractor {
    private final DistributionJobFieldExtractor fieldExtractor;

    @Autowired
    public GitHubJobDetailsExtractor(GitHubChannelKey channelKey, DistributionJobFieldExtractor fieldExtractor) {
        super(channelKey);
        this.fieldExtractor = fieldExtractor;
    }

    @Override
    public DistributionJobDetailsModel extractDetails(final UUID jobId, final Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return new GitHubJobDetailsModel(
            jobId,
            fieldExtractor.extractFieldValue(GitHubDescriptor.GITHUB_REPOSITORY_NAME, configuredFieldsMap).orElse(null),
            fieldExtractor.extractFieldValue(GitHubDescriptor.GITHUB_PR_TITLE_PREFIX, configuredFieldsMap).orElse(null)
        );
    }
}
