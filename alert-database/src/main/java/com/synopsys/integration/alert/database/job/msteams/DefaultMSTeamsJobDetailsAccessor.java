/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.msteams;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.accessor.MSTeamsJobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;

@Component
public class DefaultMSTeamsJobDetailsAccessor implements MSTeamsJobDetailsAccessor {
    private final MSTeamsJobDetailsRepository msTeamsJobDetailsRepository;

    @Autowired
    public DefaultMSTeamsJobDetailsAccessor(MSTeamsJobDetailsRepository msTeamsJobDetailsRepository) {
        this.msTeamsJobDetailsRepository = msTeamsJobDetailsRepository;
    }

    public MSTeamsJobDetailsEntity saveMSTeamsJobDetails(UUID jobId, MSTeamsJobDetailsModel msTeamsJobDetails) {
        MSTeamsJobDetailsEntity jobDetailsToSave = new MSTeamsJobDetailsEntity(jobId, msTeamsJobDetails.getWebhook());
        return msTeamsJobDetailsRepository.save(jobDetailsToSave);
    }

    @Override
    public Optional<MSTeamsJobDetailsModel> retrieveDetails(UUID jobId) {
        return msTeamsJobDetailsRepository.findById(jobId)
                   .map(entity -> new MSTeamsJobDetailsModel(entity.getJobId(), entity.getWebhook()));
    }
}
