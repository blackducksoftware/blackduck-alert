package com.blackduck.integration.alert.api.channel;

import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessageHolder;
import com.blackduck.integration.alert.common.message.model.MessageResult;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public interface DistributionChannel<D extends DistributionJobDetailsModel> {
    MessageResult distributeMessages(
        D distributionDetails,
        ProviderMessageHolder messages,
        String jobName,
        UUID jobConfigId,
        UUID jobExecutionId,
        Set<Long> notificationIds
    )
        throws AlertException;

}
