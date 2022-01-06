/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job.details;

import java.util.UUID;

import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

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
