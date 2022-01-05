/*
 * api-descriptor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.descriptor.api;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

@Component
public final class SlackChannelKey extends ChannelKey {
    private static final String COMPONENT_NAME = "channel_slack";
    private static final String SLACK_DISPLAY_NAME = "Slack";

    public SlackChannelKey() {
        super(COMPONENT_NAME, SLACK_DISPLAY_NAME);
    }

}
