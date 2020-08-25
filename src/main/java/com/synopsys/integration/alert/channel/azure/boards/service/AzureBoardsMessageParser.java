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
package com.synopsys.integration.alert.channel.azure.boards.service;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerMessageParser;

@Component
public class AzureBoardsMessageParser extends IssueTrackerMessageParser {
    public static final int TITLE_SIZE_LIMIT = 255;
    public static final int MESSAGE_SIZE_LIMIT = 30000;

    public AzureBoardsMessageParser() {
        super(TITLE_SIZE_LIMIT, MESSAGE_SIZE_LIMIT);
    }

    @Override
    protected String encodeString(String txt) {
        // TODO is this necessary?
        //  URLEncoder.encode(txt, StandardCharsets.UTF_8);
        return txt;
    }

    @Override
    protected String emphasize(String txt) {
        return String.format("<b>%s</b>", txt);
    }

    @Override
    protected String createLink(String txt, String url) {
        return String.format("<a href=\"%s\">%s</a>", url, txt);
    }

    @Override
    protected String getLineSeparator() {
        return "<br>";
    }

}
