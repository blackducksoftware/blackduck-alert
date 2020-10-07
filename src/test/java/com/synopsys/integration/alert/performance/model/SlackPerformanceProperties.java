package com.synopsys.integration.alert.performance.model;

import com.synopsys.integration.alert.channel.slack.SlackChannelKey;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;

public class SlackPerformanceProperties {
    private final String slackChannelKey;
    private final String slackChannelWebhook;
    private final String slackChannelName;
    private final String slackChannelUsername;

    public SlackPerformanceProperties() {
        this.slackChannelKey = new SlackChannelKey().getUniversalKey();

        TestProperties testProperties = new TestProperties();
        this.slackChannelWebhook = testProperties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK);
        this.slackChannelName = testProperties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME);
        this.slackChannelUsername = testProperties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME);

    }

    public String getSlackChannelKey() {
        return slackChannelKey;
    }

    public String getSlackChannelWebhook() {
        return slackChannelWebhook;
    }

    public String getSlackChannelName() {
        return slackChannelName;
    }

    public String getSlackChannelUsername() {
        return slackChannelUsername;
    }
}
