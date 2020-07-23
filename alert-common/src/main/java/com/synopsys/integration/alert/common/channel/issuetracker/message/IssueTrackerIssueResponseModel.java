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
package com.synopsys.integration.alert.common.channel.issuetracker.message;

import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class IssueTrackerIssueResponseModel extends AlertSerializableModel {
    private final AlertIssueOrigin alertIssueOrigin;
    private final String issueKey;
    private final String issueLink;
    private final String issueTitle;
    private final IssueOperation issueOperation;

    public IssueTrackerIssueResponseModel(AlertIssueOrigin alertIssueOrigin, String issueKey, String issueLink, String issueTitle, IssueOperation issueOperation) {
        this.alertIssueOrigin = alertIssueOrigin;
        this.issueKey = issueKey;
        this.issueLink = issueLink;
        this.issueTitle = issueTitle;
        this.issueOperation = issueOperation;
    }

    public AlertIssueOrigin getAlertIssueOrigin() {
        return alertIssueOrigin;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public String getIssueLink() {
        return issueLink;
    }

    public String getIssueTitle() {
        return issueTitle;
    }

    public IssueOperation getIssueOperation() {
        return issueOperation;
    }

}
