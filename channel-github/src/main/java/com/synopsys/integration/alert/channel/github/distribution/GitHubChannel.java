package com.synopsys.integration.alert.channel.github.distribution;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.DistributionChannel;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.details.GitHubJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;

@Component
public class GitHubChannel implements DistributionChannel<GitHubJobDetailsModel> {

    @Override
    public MessageResult distributeMessages(
        final GitHubJobDetailsModel distributionDetails,
        final ProviderMessageHolder messages,
        final String jobName,
        final UUID eventId,
        final Set<Long> notificationIds
    )
        throws AlertException {
        return MessageResult.success();
    }
}
