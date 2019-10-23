/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.msteams;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.channel.message.ChannelMessageParser;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

@Component
public class MsTeamsMessageParser extends ChannelMessageParser {

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
        return String.format("[%s](%s)", txt, url);
    }

    @Override
    protected String getLineSeparator() {
        return "\r\n";
    }

    @Override
    protected String getSectionSeparator() {
        return "";
    }

    @Override
    public String createHeader(MessageContentGroup messageContentGroup) {
        return "";
    }

    @Override
    public String getCommonTopic(MessageContentGroup messageContentGroup) {
        return "";
    }

    @Override
    public String getComponentSubTopic(ProviderMessageContent messageContent) {
        return "{\n"
                   + "    \"startGroup\": true,\n"
                   + "    \"title\": \""
                   + super.getComponentSubTopic(messageContent)
                   + "\",";
    }

    @Override
    public String createComponentItemMessage(ProviderMessageContent messageContent) {
        return "    \"text\": \""
                   + super.createComponentItemMessage(messageContent)
                   + "\"    },";
    }

    @Override
    public String createFooter(MessageContentGroup messageContentGroup) {
        return "";
    }
}
