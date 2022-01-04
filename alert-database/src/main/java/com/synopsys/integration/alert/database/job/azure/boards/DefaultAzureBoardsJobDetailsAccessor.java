/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.azure.boards;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.accessor.AzureBoardsJobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;

@Component
public class DefaultAzureBoardsJobDetailsAccessor implements AzureBoardsJobDetailsAccessor {
    private final AzureBoardsJobDetailsRepository azureBoardsJobDetailsRepository;

    @Autowired
    public DefaultAzureBoardsJobDetailsAccessor(AzureBoardsJobDetailsRepository azureBoardsJobDetailsRepository) {
        this.azureBoardsJobDetailsRepository = azureBoardsJobDetailsRepository;
    }

    @Override
    public Optional<AzureBoardsJobDetailsModel> retrieveDetails(UUID jobId) {
        return azureBoardsJobDetailsRepository.findById(jobId).map(this::convertToModel);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AzureBoardsJobDetailsEntity saveAzureBoardsJobDetails(UUID jobId, AzureBoardsJobDetailsModel azureBoardsJobDetails) {
        AzureBoardsJobDetailsEntity detailsToSave = new AzureBoardsJobDetailsEntity(
            jobId,
            azureBoardsJobDetails.isAddComments(),
            azureBoardsJobDetails.getProjectNameOrId(),
            azureBoardsJobDetails.getWorkItemType(),
            azureBoardsJobDetails.getWorkItemCompletedState(),
            azureBoardsJobDetails.getWorkItemReopenState()
        );
        return azureBoardsJobDetailsRepository.save(detailsToSave);
    }

    private AzureBoardsJobDetailsModel convertToModel(AzureBoardsJobDetailsEntity jobDetails) {
        return new AzureBoardsJobDetailsModel(
            jobDetails.getJobId(),
            jobDetails.getAddComments(),
            jobDetails.getProjectNameOrId(),
            jobDetails.getWorkItemType(),
            jobDetails.getWorkItemCompletedState(),
            jobDetails.getWorkItemReopenState()
        );
    }

}
