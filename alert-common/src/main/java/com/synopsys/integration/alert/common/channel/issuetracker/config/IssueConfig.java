/**
 * alert-common
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
package com.synopsys.integration.alert.common.channel.issuetracker.config;

import java.util.Optional;

public class IssueConfig {
    private String projectName;
    private String projectKey;
    private String projectId;
    private String issueCreator;
    private String issueType;
    private boolean commentOnIssues;
    private String resolveTransition;
    private String openTransition;

    public IssueConfig() {
        // For serialization
    }

    public IssueConfig(
        String projectName
        , String projectKey
        , String projectId
        , String issueCreator
        , String issueType
        , boolean commentOnIssues
        , String resolveTransition
        , String openTransition
    ) {
        this.projectName = projectName;
        this.projectKey = projectKey;
        this.projectId = projectId;
        this.issueCreator = issueCreator;
        this.issueType = issueType;
        this.commentOnIssues = commentOnIssues;
        this.resolveTransition = resolveTransition;
        this.openTransition = openTransition;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getIssueCreator() {
        return issueCreator;
    }

    public void setIssueCreator(String issueCreator) {
        this.issueCreator = issueCreator;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public boolean getCommentOnIssues() {
        return commentOnIssues;
    }

    public void setCommentOnIssues(boolean commentOnIssues) {
        this.commentOnIssues = commentOnIssues;
    }

    public Optional<String> getResolveTransition() {
        return Optional.ofNullable(resolveTransition);
    }

    public void setResolveTransition(String resolveTransition) {
        this.resolveTransition = resolveTransition;
    }

    public Optional<String> getOpenTransition() {
        return Optional.ofNullable(openTransition);
    }

    public void setOpenTransition(String openTransition) {
        this.openTransition = openTransition;
    }

}
