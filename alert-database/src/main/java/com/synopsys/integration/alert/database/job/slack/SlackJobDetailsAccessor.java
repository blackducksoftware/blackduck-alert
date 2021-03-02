/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.slack;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;

@Component
public class SlackJobDetailsAccessor {
    private final SlackJobDetailsRepository slackJobDetailsRepository;

    @Autowired
    public SlackJobDetailsAccessor(SlackJobDetailsRepository slackJobDetailsRepository) {
        this.slackJobDetailsRepository = slackJobDetailsRepository;
    }

    public SlackJobDetailsEntity saveSlackJobDetails(UUID jobId, SlackJobDetailsModel slackJobDetails) {
        SlackJobDetailsEntity jobDetailsToSave = new SlackJobDetailsEntity(jobId, slackJobDetails.getWebhook(), slackJobDetails.getChannelName(), slackJobDetails.getChannelUsername());
        return slackJobDetailsRepository.save(jobDetailsToSave);
    }

}
