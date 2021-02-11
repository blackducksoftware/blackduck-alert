/*
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
package com.synopsys.integration.alert.channel.slack2;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;
import com.synopys.integration.alert.channel.api.convert.ChannelMessageFormatter;

@Component
public class SlackChannelMessageFormatter extends ChannelMessageFormatter {
    private static final int MAX_SLACK_MESSAGE_LENGTH = 3500;
    private static final String SLACK_LINE_SEPARATOR = "\n";
    private static final Map<Character, String> SLACK_CHARACTER_ENCODING_MAP = new LinkedHashMap<>();

    private final MarkupEncoderUtil markupEncoderUtil;

    static {
        // Insertion order matters, so '&' must always be inserted first.
        SLACK_CHARACTER_ENCODING_MAP.put('&', "&amp;");
        SLACK_CHARACTER_ENCODING_MAP.put('<', "&lt;");
        SLACK_CHARACTER_ENCODING_MAP.put('>', "&gt;");
    }

    @Autowired
    public SlackChannelMessageFormatter(MarkupEncoderUtil markupEncoderUtil) {
        super(MAX_SLACK_MESSAGE_LENGTH, SLACK_LINE_SEPARATOR);
        this.markupEncoderUtil = markupEncoderUtil;
    }

    @Override
    public String encode(String txt) {
        return markupEncoderUtil.encodeMarkup(SLACK_CHARACTER_ENCODING_MAP, txt);
    }

    @Override
    public String emphasize(String txt) {
        return String.format("*%s*", txt);
    }

    @Override
    protected String createLink(String txt, String url) {
        return String.format("<%s|%s>", url, txt);
    }
    
}
