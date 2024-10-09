package com.blackduck.integration.alert.api.descriptor;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKey;

@Component
public final class SlackChannelKey extends ChannelKey {
    private static final String COMPONENT_NAME = "channel_slack";
    private static final String SLACK_DISPLAY_NAME = "Slack";

    public SlackChannelKey() {
        super(COMPONENT_NAME, SLACK_DISPLAY_NAME);
    }

}
