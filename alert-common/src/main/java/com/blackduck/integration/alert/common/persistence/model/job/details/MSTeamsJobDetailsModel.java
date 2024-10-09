package com.blackduck.integration.alert.common.persistence.model.job.details;

import java.util.UUID;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;

public class MSTeamsJobDetailsModel extends DistributionJobDetailsModel {
    private final String webhook;

    public MSTeamsJobDetailsModel(UUID jobId, String webhook) {
        super(ChannelKeys.MS_TEAMS, jobId);
        this.webhook = webhook;
    }

    public String getWebhook() {
        return webhook;
    }

}
