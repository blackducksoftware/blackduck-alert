package com.synopsys.integration.alert.database.deprecated.channel.slack;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.deprecated.channel.DistributionChannelConfigEntity;

@Entity
@Table(schema = "alert", name = "slack_distribution_config")
public class SlackDistributionConfigEntity extends DistributionChannelConfigEntity {
    @Column(name = "webhook")
    private String webhook;

    @Column(name = "channel_username")
    private String channelUsername = "BlackDuck-Alert";

    @Column(name = "channel_name")
    private String channelName;

    public SlackDistributionConfigEntity() {
        // JPA requires default constructor definitions
    }

    public SlackDistributionConfigEntity(final String webhook, final String channelUsername, final String channelName) {
        this.webhook = webhook;
        this.channelUsername = channelUsername;
        this.channelName = channelName;
    }

    public String getWebhook() {
        return webhook;
    }

    public String getChannelUsername() {
        return channelUsername;
    }

    public String getChannelName() {
        return channelName;
    }

}