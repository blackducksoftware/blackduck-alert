/*
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
package com.synopsys.integration.alert.channel.jira.common;

import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;

public abstract class JiraContextBuilder<T> {
    protected abstract String getProjectFieldKey();

    protected abstract String getIssueTypeFieldKey();

    protected abstract String getIssueCreatorFieldKey();

    protected abstract String getAddCommentsFieldKey();

    protected abstract String getResolveTransitionFieldKey();

    protected abstract String getOpenTransitionFieldKey();

    protected abstract String getDefaultIssueCreatorFieldKey();

    public abstract T build(FieldUtility fieldUtility);

    protected IssueConfig createIssueConfig(FieldUtility fieldUtility) {
        String projectName = fieldUtility.getStringOrNull(getProjectFieldKey());
        String issueCreator = fieldUtility.getString(getIssueCreatorFieldKey()).orElseGet(() -> fieldUtility.getStringOrNull(getDefaultIssueCreatorFieldKey()));
        String issueType = fieldUtility.getString(getIssueTypeFieldKey()).orElse(JiraConstants.DEFAULT_ISSUE_TYPE);
        Boolean commentOnIssues = fieldUtility.getBooleanOrFalse(getAddCommentsFieldKey());
        String resolveTransition = fieldUtility.getStringOrNull(getResolveTransitionFieldKey());
        String openTransition = fieldUtility.getStringOrNull(getOpenTransitionFieldKey());

        IssueConfig issueConfig = new IssueConfig();
        issueConfig.setProjectName(projectName);
        issueConfig.setIssueCreator(issueCreator);
        issueConfig.setIssueType(issueType);
        issueConfig.setCommentOnIssues(commentOnIssues);
        issueConfig.setResolveTransition(resolveTransition);
        issueConfig.setOpenTransition(openTransition);

        return issueConfig;
    }
}
