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
package com.synopsys.integration.alert.channel.msteams2;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.api.convert.ChannelMessageFormatter;
import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;

@Component
public class MSTeamsChannelMessageFormatter extends ChannelMessageFormatter {
    // There is a size limit in the request size that is allowed (20KB). This text limit is meant to hopefully keep the message under that size limit
    private static final int MAX_MSTEAMS_MESSAGE_LENGTH = 10000;
    private static final String MSTEAMS_LINE_SEPARATOR = "\r\n\r\n";
    private static final Map<Character, String> MSTEAMS_CHARACTER_ENCODING_MAP = Map.of(
        '*', "\\*",
        '~', "\\~",
        '#', "\\#",
        '-', "\\-",
        '_', "\\_"
    );

    private final MarkupEncoderUtil markupEncoderUtil;

    @Autowired
    public MSTeamsChannelMessageFormatter(MarkupEncoderUtil markupEncoderUtil) {
        super(MAX_MSTEAMS_MESSAGE_LENGTH, MSTEAMS_LINE_SEPARATOR);
        this.markupEncoderUtil = markupEncoderUtil;
    }

    @Override
    public String encode(String txt) {
        return markupEncoderUtil.encodeMarkup(MSTEAMS_CHARACTER_ENCODING_MAP, txt);
    }

    @Override
    public String emphasize(String txt) {
        return String.format("**%s**", txt);
    }

    @Override
    public String createLink(String txt, String url) {
        return String.format("[%s](%s)", txt, url);
    }

}
