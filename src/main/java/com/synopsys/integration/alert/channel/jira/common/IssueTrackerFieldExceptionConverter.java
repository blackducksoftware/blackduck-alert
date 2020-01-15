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

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraDescriptor;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.issuetracker.common.exception.IssueTrackerFieldException;
import com.synopsys.integration.issuetracker.jira.cloud.JiraCloudProperties;
import com.synopsys.integration.issuetracker.jira.server.JiraServerProperties;

@Component
public class IssueTrackerFieldExceptionConverter {
    private final Map<String, String> keyLookupMap = Map.of(
        JiraCloudProperties.KEY_ISSUE_CREATOR, JiraDescriptor.KEY_ISSUE_CREATOR,
        JiraCloudProperties.KEY_JIRA_PROJECT_NAME, JiraDescriptor.KEY_JIRA_PROJECT_NAME,
        JiraCloudProperties.KEY_ISSUE_TYPE, JiraDescriptor.KEY_ISSUE_TYPE,
        JiraCloudProperties.KEY_OPEN_WORKFLOW_TRANSITION, JiraDescriptor.KEY_OPEN_WORKFLOW_TRANSITION,
        JiraCloudProperties.KEY_RESOLVE_WORKFLOW_TRANSITION, JiraDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION,
        JiraServerProperties.KEY_ISSUE_CREATOR, JiraServerDescriptor.KEY_ISSUE_CREATOR,
        JiraServerProperties.KEY_JIRA_PROJECT_NAME, JiraServerDescriptor.KEY_JIRA_PROJECT_NAME,
        JiraServerProperties.KEY_ISSUE_TYPE, JiraServerDescriptor.KEY_ISSUE_TYPE,
        JiraServerProperties.KEY_OPEN_WORKFLOW_TRANSITION, JiraServerDescriptor.KEY_OPEN_WORKFLOW_TRANSITION,
        JiraServerProperties.KEY_RESOLVE_WORKFLOW_TRANSITION, JiraServerDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION);

    public final AlertFieldException convert(IssueTrackerFieldException ex) {
        Map<String, String> errorsMap = new HashMap<>();
        for (Map.Entry<String, String> errorEntry : ex.getFieldErrors().entrySet()) {
            String errorKey = errorEntry.getKey();
            String key = keyLookupMap.getOrDefault(errorKey, errorKey);
            errorsMap.put(key, errorEntry.getValue());
        }

        return new AlertFieldException(ex.getMessage(), errorsMap);
    }

}
