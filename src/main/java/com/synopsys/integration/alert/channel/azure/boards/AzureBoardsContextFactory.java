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

    protected String getProjectFieldKey() {
        return AzureBoardsDescriptor.KEY_AZURE_PROJECT;
    }

    protected String getIssueTypeFieldKey() {
        return AzureBoardsDescriptor.KEY_WORK_ITEM_TYPE;
    }

    protected String getIssueCreatorFieldKey() {
        return AzureBoardsDescriptor.KEY_WORK_ITEM_CREATOR_EMAIL;
    }

    protected String getAddCommentsFieldKey() {
        return AzureBoardsDescriptor.KEY_WORK_ITEM_COMMENT;
    }

    protected String getResolveTransitionFieldKey() {
        return AzureBoardsDescriptor.KEY_WORK_ITEM_COMPLETED_STATE;
    }

    protected String getOpenTransitionFieldKey() {
        return AzureBoardsDescriptor.KEY_WORK_ITEM_REOPEN_STATE;
    }

    protected String getDefaultIssueCreatorFieldKey() {
        //TODO: We may need to expose a default issue creator email address in the global config
        return AzureBoardsDescriptor.KEY_WORK_ITEM_CREATOR_EMAIL;
    }

    public AzureBoardsContext build(FieldAccessor fieldAccessor) {
        return new AzureBoardsContext(createAzureBoardsProperties(fieldAccessor), createIssueConfig(fieldAccessor));
    }

    protected IssueConfig createIssueConfig(FieldAccessor fieldAccessor) {
        String projectName = fieldAccessor.getStringOrNull(getProjectFieldKey());
        String issueCreator = fieldAccessor.getString(getIssueCreatorFieldKey()).orElseGet(() -> fieldAccessor.getStringOrNull(getDefaultIssueCreatorFieldKey()));
        String issueType = fieldAccessor.getString(getIssueTypeFieldKey()).orElse(AzureBoardsConstants.DEFAULT_WORK_ITEM_TYPE);
        Boolean commentOnIssues = fieldAccessor.getBooleanOrFalse(getAddCommentsFieldKey());
        String resolveTransition = fieldAccessor.getStringOrNull(getResolveTransitionFieldKey());
        String openTransition = fieldAccessor.getStringOrNull(getOpenTransitionFieldKey());

        IssueConfig issueConfig = new IssueConfig();
        issueConfig.setProjectName(projectName);
        issueConfig.setIssueCreator(issueCreator);
        issueConfig.setIssueType(issueType);
        issueConfig.setCommentOnIssues(commentOnIssues);
        issueConfig.setResolveTransition(resolveTransition);
        issueConfig.setOpenTransition(openTransition);

        return issueConfig;
    }

    private AzureBoardsProperties createAzureBoardsProperties(FieldAccessor fieldAccessor) {
        return AzureBoardsProperties.fromFieldAccessor(azureBoardsCredentialDataStoreFactory, azureRedirectUtil.createOAuthRedirectUri(), fieldAccessor);
    }
}
