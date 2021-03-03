/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job.details;

import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

public class SlackJobDetailsModel extends DistributionJobDetailsModel {
    private final String webhook;
    private final String channelName;
    private final String channelUsername;

    public SlackJobDetailsModel(String webhook, String channelName, String channelUsername) {
        super(ChannelKey.SLACK);
        this.webhook = webhook;
        this.channelName = channelName;
        this.channelUsername = channelUsername;
    }

    public String getWebhook() {
        return webhook;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getChannelUsername() {
        return channelUsername;
    }

}
