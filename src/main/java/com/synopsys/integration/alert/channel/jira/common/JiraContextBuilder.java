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
package com.synopsys.integration.alert.channel.jira.common;

import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.issuetracker.config.IssueConfig;
import com.synopsys.integration.issuetracker.jira.common.JiraConstants;

public abstract class JiraContextBuilder<T> {
    protected abstract String getProjectFieldKey();

    protected abstract String getIssueTypeFieldKey();

    protected abstract String getIssueCreatorFieldKey();

    protected abstract String getAddCommentsFieldKey();

    protected abstract String getResolveTransitionFieldKey();

    protected abstract String getOpenTransitionFieldKey();

    protected abstract String getDefaultIssueCreatorFieldKey();

    public abstract T build(FieldAccessor fieldAccessor);

    protected IssueConfig createIssueConfig(FieldAccessor fieldAccessor) {
        String projectName = fieldAccessor.getStringOrNull(getProjectFieldKey());
        String issueCreator = fieldAccessor.getString(getIssueCreatorFieldKey()).orElseGet(() -> fieldAccessor.getStringOrNull(getDefaultIssueCreatorFieldKey()));
        String issueType = fieldAccessor.getString(getIssueTypeFieldKey()).orElse(JiraConstants.DEFAULT_ISSUE_TYPE);
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
}
