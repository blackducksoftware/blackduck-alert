/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job.details;

import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

public class MSTeamsJobDetailsModel extends DistributionJobDetailsModel {
    private final String webhook;

    public MSTeamsJobDetailsModel(String webhook) {
        super(ChannelKey.MS_TEAMS);
        this.webhook = webhook;
    }

    public String getWebhook() {
        return webhook;
    }

}
