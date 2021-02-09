/**
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
package com.synopsys.integration.alert.channel.slack.parser;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.channel.message.ChannelMessageParser;
import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;

@Component
public class SlackChannelMessageParser extends ChannelMessageParser {
    private static final Map<Character, String> SLACK_CHARACTER_ENCODING_MAP;

    private MarkupEncoderUtil markupEncoderUtil;

    static {
        // Insertion order matters, so '&' must always be inserted first.
        SLACK_CHARACTER_ENCODING_MAP = new LinkedHashMap<>();
        SLACK_CHARACTER_ENCODING_MAP.put('&', "&amp;");
        SLACK_CHARACTER_ENCODING_MAP.put('<', "&lt;");
        SLACK_CHARACTER_ENCODING_MAP.put('>', "&gt;");
    }

    @Autowired
    public SlackChannelMessageParser(MarkupEncoderUtil markupEncoderUtil) {
        this.markupEncoderUtil = markupEncoderUtil;
    }

    @Override
    protected String encodeString(String txt) {
        return markupEncoderUtil.encodeMarkup(SLACK_CHARACTER_ENCODING_MAP, txt);
    }

    @Override
    protected String emphasize(String txt) {
        return String.format("*%s*", txt);
    }

    @Override
    protected String createLink(String txt, String url) {
        return String.format("<%s|%s>", url, txt);
    }

    @Override
    protected String getLineSeparator() {
        return "\n";
    }

}
