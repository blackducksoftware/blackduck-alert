package com.synopsys.integration.alert.descriptor.api;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

//TODO this entire class is just for a test, we should remove this and it should not be merged into master
@Component
public final class SlackChannelKeyV2 extends ChannelKey {
    private static final String COMPONENT_NAME = "channel_slack_v2";
    private static final String SLACK_DISPLAY_NAME = "Slack";

    public SlackChannelKeyV2() {
        super(COMPONENT_NAME, SLACK_DISPLAY_NAME);
    }
}