package com.blackduck.integration.alert.database.job.slack;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "alert", name = "slack_job_details")
public class SlackJobDetailsEntity {
    @Id
    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "webhook")
    private String webhook;

    @Column(name = "channel_username")
    private String channelUsername;

    public SlackJobDetailsEntity() {
    }

    public SlackJobDetailsEntity(UUID jobId, String webhook, String channelUsername) {
        this.jobId = jobId;
        this.webhook = webhook;
        this.channelUsername = channelUsername;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    public String getChannelUsername() {
        return channelUsername;
    }

    public void setChannelUsername(String channelUsername) {
        this.channelUsername = channelUsername;
    }

}
