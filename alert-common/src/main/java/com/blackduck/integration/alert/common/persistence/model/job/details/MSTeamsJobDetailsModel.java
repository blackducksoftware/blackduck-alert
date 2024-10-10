/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
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
