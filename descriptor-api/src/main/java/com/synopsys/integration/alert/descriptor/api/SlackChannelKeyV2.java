/*
 * descriptor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.descriptor.api;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

//FIXME: these ChannelKeys are used for testing the NotificationReceiverV2. These must be removed before releasing 6.5.0.
@Component
public final class SlackChannelKeyV2 extends ChannelKey {
    private static final String COMPONENT_NAME = "channel_slack_v2";
    private static final String SLACK_DISPLAY_NAME = "Slack";

    public SlackChannelKeyV2() {
        super(COMPONENT_NAME, SLACK_DISPLAY_NAME);
    }
}