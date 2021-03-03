/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.channel.azure.boards.oauth.storage.AzureBoardsCredentialDataStoreFactory;
import com.synopsys.integration.alert.channel.azure.boards.service.AzureBoardsProperties;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;

@Component
public class AzureBoardsContextFactory {
    private final AzureBoardsCredentialDataStoreFactory azureBoardsCredentialDataStoreFactory;
    private final AzureRedirectUtil azureRedirectUtil;

    @Autowired
    public AzureBoardsContextFactory(AzureBoardsCredentialDataStoreFactory azureBoardsCredentialDataStoreFactory, AzureRedirectUtil azureRedirectUtil) {
        this.azureBoardsCredentialDataStoreFactory = azureBoardsCredentialDataStoreFactory;
        this.azureRedirectUtil = azureRedirectUtil;
    }

    public AzureBoardsContext build(FieldUtility fieldUtility) {
        return new AzureBoardsContext(createAzureBoardsProperties(fieldUtility), createIssueConfig(fieldUtility));
    }

    public AzureBoardsContext fromConfig(ConfigurationModel azureBoardsGlobalConfig, AzureBoardsJobDetailsModel jobDetails) {
        AzureBoardsProperties azureBoardsProperties = AzureBoardsProperties.fromGlobalConfig(azureBoardsCredentialDataStoreFactory, azureRedirectUtil.createOAuthRedirectUri(), azureBoardsGlobalConfig);

        IssueConfig issueConfig = new IssueConfig(
            jobDetails.getProjectNameOrId(),
            null,
            null,
            null,
            jobDetails.getWorkItemType(),
            jobDetails.isAddComments(),
            jobDetails.getWorkItemCompletedState(),
            jobDetails.getWorkItemReopenState()
        );
        return new AzureBoardsContext(azureBoardsProperties, issueConfig);
    }

    private IssueConfig createIssueConfig(FieldUtility fieldUtility) {
        String projectName = getFieldString(fieldUtility, AzureBoardsDescriptor.KEY_AZURE_PROJECT);
        String issueType = getFieldString(fieldUtility, AzureBoardsDescriptor.KEY_WORK_ITEM_TYPE);
        boolean commentOnIssues = fieldUtility.getBooleanOrFalse(AzureBoardsDescriptor.KEY_WORK_ITEM_COMMENT);
        String resolveTransition = getFieldString(fieldUtility, AzureBoardsDescriptor.KEY_WORK_ITEM_COMPLETED_STATE);
        String openTransition = getFieldString(fieldUtility, AzureBoardsDescriptor.KEY_WORK_ITEM_REOPEN_STATE);

        return new IssueConfig(
            projectName,
            null,
            null,
            null,
            issueType,
            commentOnIssues,
            resolveTransition,
            openTransition
        );
    }

    private String getFieldString(FieldUtility fieldUtility, String fieldKey) {
        return fieldUtility.getStringOrNull(fieldKey);
    }

    private AzureBoardsProperties createAzureBoardsProperties(FieldUtility fieldUtility) {
        return AzureBoardsProperties.fromFieldAccessor(azureBoardsCredentialDataStoreFactory, azureRedirectUtil.createOAuthRedirectUri(), fieldUtility);
    }

}
