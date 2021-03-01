/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.azure.boards;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;

@Component
public class AzureBoardsJobDetailsAccessor {
    private final AzureBoardsJobDetailsRepository azureBoardsJobDetailsRepository;

    @Autowired
    public AzureBoardsJobDetailsAccessor(AzureBoardsJobDetailsRepository azureBoardsJobDetailsRepository) {
        this.azureBoardsJobDetailsRepository = azureBoardsJobDetailsRepository;
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

}
