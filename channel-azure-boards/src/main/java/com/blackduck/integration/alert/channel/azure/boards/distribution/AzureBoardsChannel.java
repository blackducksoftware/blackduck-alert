package com.blackduck.integration.alert.channel.azure.boards.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerChannel;
import com.blackduck.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;

@Component
public class AzureBoardsChannel extends IssueTrackerChannel<AzureBoardsJobDetailsModel, Integer> {
    @Autowired
    public AzureBoardsChannel(AzureBoardsProcessorFactory processorFactory) {
        super(processorFactory);
    }

}
