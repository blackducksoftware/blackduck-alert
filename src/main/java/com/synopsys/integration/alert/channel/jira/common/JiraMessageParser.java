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

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerMessageParser;

@Component
public class JiraMessageParser extends IssueTrackerMessageParser {
    @Override
    protected String encodeString(String txt) {
        return txt;
    }

    @Override
    protected String emphasize(String txt) {
        return String.format("*%s*", txt);
    }

    @Override
    protected String createLink(String txt, String url) {
        // Jira uses [] for link content the trailing ] is removed on Jira Server; add whitespace before and after the brackets to fix and be consistent for Jira Server and Jira Cloud.
        return String.format(" [%s|%s] ", txt, url);
    }

    @Override
    protected String getLineSeparator() {
        return "\n";
    }

    @Override
    protected String getSectionSeparator() {
        return "";
    }

    @Override
    protected int getTitleSizeLimit() {
        return 255;
    }

    @Override
    protected int getMessageSizeLimit() {
        return 30000;
    }

}
