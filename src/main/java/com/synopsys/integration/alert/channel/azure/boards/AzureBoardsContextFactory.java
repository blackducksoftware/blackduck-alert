/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.channel.azure.boards.oauth.storage.AzureBoardsCredentialDataStoreFactory;
import com.synopsys.integration.alert.channel.azure.boards.service.AzureBoardsProperties;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;

@Component
public class AzureBoardsContextFactory {
    private final AzureBoardsCredentialDataStoreFactory azureBoardsCredentialDataStoreFactory;
    private final AzureRedirectUtil azureRedirectUtil;

    @Autowired
    public AzureBoardsContextFactory(AzureBoardsCredentialDataStoreFactory azureBoardsCredentialDataStoreFactory, AzureRedirectUtil azureRedirectUtil) {
        this.azureBoardsCredentialDataStoreFactory = azureBoardsCredentialDataStoreFactory;
        this.azureRedirectUtil = azureRedirectUtil;
    }

    public AzureBoardsContext build(FieldAccessor fieldAccessor) {
        return new AzureBoardsContext(createAzureBoardsProperties(fieldAccessor), createIssueConfig(fieldAccessor));
    }

    private IssueConfig createIssueConfig(FieldAccessor fieldAccessor) {
        String projectName = getFieldString(fieldAccessor, AzureBoardsDescriptor.KEY_AZURE_PROJECT);
        String issueType = getFieldString(fieldAccessor, AzureBoardsDescriptor.KEY_WORK_ITEM_TYPE);
        Boolean commentOnIssues = fieldAccessor.getBooleanOrFalse(AzureBoardsDescriptor.KEY_WORK_ITEM_COMMENT);
        String resolveTransition = getFieldString(fieldAccessor, AzureBoardsDescriptor.KEY_WORK_ITEM_COMPLETED_STATE);
        String openTransition = getFieldString(fieldAccessor, AzureBoardsDescriptor.KEY_WORK_ITEM_REOPEN_STATE);

        IssueConfig issueConfig = new IssueConfig();
        issueConfig.setProjectName(projectName);
        issueConfig.setIssueType(issueType);
        issueConfig.setCommentOnIssues(commentOnIssues);
        issueConfig.setResolveTransition(resolveTransition);
        issueConfig.setOpenTransition(openTransition);

        return issueConfig;
    }

    private String getFieldString(FieldAccessor fieldAccessor, String fieldKey) {
        return fieldAccessor.getStringOrNull(fieldKey);
    }

    private AzureBoardsProperties createAzureBoardsProperties(FieldAccessor fieldAccessor) {
        return AzureBoardsProperties.fromFieldAccessor(azureBoardsCredentialDataStoreFactory, azureRedirectUtil.createOAuthRedirectUri(), fieldAccessor);
    }
}
