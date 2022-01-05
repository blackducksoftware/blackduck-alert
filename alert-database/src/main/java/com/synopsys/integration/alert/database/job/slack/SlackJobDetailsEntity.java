/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.slack;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema = "alert", name = "slack_job_details")
public class SlackJobDetailsEntity {
    @Id
    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "webhook")
    private String webhook;

    @Column(name = "channel_name")
    private String channelName;

    @Column(name = "channel_username")
    private String channelUsername;

    public SlackJobDetailsEntity() {
    }

    public SlackJobDetailsEntity(UUID jobId, String webhook, String channelName, String channelUsername) {
        this.jobId = jobId;
        this.webhook = webhook;
        this.channelName = channelName;
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

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelUsername() {
        return channelUsername;
    }

    public void setChannelUsername(String channelUsername) {
        this.channelUsername = channelUsername;
    }

}
