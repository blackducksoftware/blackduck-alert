/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.model.job.details;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;

public class SlackJobDetailsModel extends DistributionJobDetailsModel {
    private final String webhook;
    private final String channelUsername;

    // TODO: Make channelUsername @Nullable since it is an optional field. This would additional validation in SlackChannelMessageSender if it wasn't trimmed to null - JM
    public SlackJobDetailsModel(UUID jobId, String webhook, String channelUsername) {
        super(ChannelKeys.SLACK, jobId);
        this.webhook = webhook;
        this.channelUsername = StringUtils.trimToNull(channelUsername);
    }

    public String getWebhook() {
        return webhook;
    }

    public String getChannelUsername() {
        return channelUsername;
    }

}
