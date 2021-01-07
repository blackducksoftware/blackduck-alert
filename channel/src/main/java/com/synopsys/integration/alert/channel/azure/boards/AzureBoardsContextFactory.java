/**
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.channel.azure.boards;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    private String getFieldString(FieldUtility fieldUtility, String fieldKey) {
        return fieldUtility.getStringOrNull(fieldKey);
    }

}
