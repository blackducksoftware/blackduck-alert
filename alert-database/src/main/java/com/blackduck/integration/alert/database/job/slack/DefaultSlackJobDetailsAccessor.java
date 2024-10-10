/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.slack;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.blackduck.integration.alert.common.persistence.accessor.SlackJobDetailsAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;

@Component
public class DefaultSlackJobDetailsAccessor implements SlackJobDetailsAccessor {
    private final SlackJobDetailsRepository slackJobDetailsRepository;

    @Autowired
    public DefaultSlackJobDetailsAccessor(SlackJobDetailsRepository slackJobDetailsRepository) {
        this.slackJobDetailsRepository = slackJobDetailsRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public SlackJobDetailsEntity saveSlackJobDetails(UUID jobId, SlackJobDetailsModel slackJobDetails) {
        SlackJobDetailsEntity jobDetailsToSave = new SlackJobDetailsEntity(jobId, slackJobDetails.getWebhook(), slackJobDetails.getChannelUsername());
        return slackJobDetailsRepository.save(jobDetailsToSave);
    }

    @Override
    public Optional<SlackJobDetailsModel> retrieveDetails(UUID jobId) {
        return slackJobDetailsRepository.findById(jobId)
                   .map(slackEntity -> new SlackJobDetailsModel(
                       slackEntity.getJobId(),
                       slackEntity.getWebhook(),
                       slackEntity.getChannelUsername()
                   ));
    }
}
