/**
 * provider
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
package com.synopsys.integration.alert.provider.blackduck.issue;

public class BlackDuckProviderIssueModel {
    public static final String DEFAULT_ASSIGNEE = "Alert User";

    private final String key;
    private final String status;
    private final String summary;
    private final String link;

    private String assignee = DEFAULT_ASSIGNEE;

    public BlackDuckProviderIssueModel(String key, String status, String summary, String link) {
        this.key = key;
        this.status = status;
        this.summary = summary;
        this.link = link;
    }

    public String getKey() {
        return key;
    }

    public String getStatus() {
        return status;
    }

    public String getSummary() {
        return summary;
    }

    public String getLink() {
        return link;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

}
