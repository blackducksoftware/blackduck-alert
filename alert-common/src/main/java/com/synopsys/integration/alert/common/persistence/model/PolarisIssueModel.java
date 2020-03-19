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
package com.synopsys.integration.alert.common.persistence.model;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class PolarisIssueModel extends AlertSerializableModel {
    private final String issueType;
    private final Integer previousIssueCount;
    private final Integer currentIssueCount;

    public static PolarisIssueModel createNewIssue(final String issueType, final Integer issueCount) {
        return new PolarisIssueModel(issueType, 0, issueCount);
    }

    public PolarisIssueModel(final String issueType, final Integer previousIssueCount, final Integer currentIssueCount) {
        this.issueType = issueType;
        this.previousIssueCount = previousIssueCount;
        this.currentIssueCount = currentIssueCount;
    }

    public String getIssueType() {
        return issueType;
    }

    public Integer getPreviousIssueCount() {
        return previousIssueCount;
    }

    public Integer getCurrentIssueCount() {
        return currentIssueCount;
    }

    public boolean isIssueCountIncreasing() {
        return previousIssueCount.intValue() < currentIssueCount.intValue();
    }

    public boolean isIssueCountDecreasing() {
        return previousIssueCount.intValue() > currentIssueCount.intValue();
    }

}
