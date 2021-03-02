/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.msteams;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;

@Component
public class MSTeamsJobDetailsAccessor {
    private final MSTeamsJobDetailsRepository msTeamsJobDetailsRepository;

    @Autowired
    public MSTeamsJobDetailsAccessor(MSTeamsJobDetailsRepository msTeamsJobDetailsRepository) {
        this.msTeamsJobDetailsRepository = msTeamsJobDetailsRepository;
    }

    public MSTeamsJobDetailsEntity saveMSTeamsJobDetails(UUID jobId, MSTeamsJobDetailsModel msTeamsJobDetails) {
        MSTeamsJobDetailsEntity jobDetailsToSave = new MSTeamsJobDetailsEntity(jobId, msTeamsJobDetails.getWebhook());
        return msTeamsJobDetailsRepository.save(jobDetailsToSave);
    }

}
