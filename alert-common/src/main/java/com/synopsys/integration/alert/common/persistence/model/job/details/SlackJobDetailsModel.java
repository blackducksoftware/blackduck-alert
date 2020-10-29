package com.synopsys.integration.alert.common.persistence.model.job.details;

public class SlackJobDetailsModel extends DistributionJobDetailsModel {
    private final String webhook;
    private final String channelName;
    private final String channelUsername;

    public SlackJobDetailsModel(String webhook, String channelName, String channelUsername) {
        super("channel_slack");
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
