/*
 * channel-api
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
package com.synopys.integration.alert.channel.api.convert;

import java.util.List;

import com.synopsys.integration.alert.common.channel.message.ChunkedStringBuilder;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;

public class SimpleMessageConverter extends ProviderMessageConverter<SimpleMessage> {
    private final ChannelMessageFormatter messageFormatter;

    public SimpleMessageConverter(ChannelMessageFormatter messageFormatter) {
        super(messageFormatter);
        this.messageFormatter = messageFormatter;
    }

    @Override
    public List<String> convertToFormattedMessageChunks(SimpleMessage simpleMessage) {
        ChunkedStringBuilder chunkedStringBuilder = new ChunkedStringBuilder(messageFormatter.getMaxMessageLength());

        appendSection(chunkedStringBuilder, simpleMessage.getSummary());
        appendSection(chunkedStringBuilder, simpleMessage.getDescription());

        for (LinkableItem detail : simpleMessage.getDetails()) {
            String detailString = createLinkableItemString(detail, false);
            chunkedStringBuilder.append(detailString);
            chunkedStringBuilder.append(messageFormatter.getLineSeparator());
        }

        return chunkedStringBuilder.collectCurrentChunks();
    }

    private void appendSection(ChunkedStringBuilder chunkedStringBuilder, String txt) {
        String encodedTxt = messageFormatter.encode(txt);
        chunkedStringBuilder.append(encodedTxt);
        chunkedStringBuilder.append(messageFormatter.getLineSeparator());
        chunkedStringBuilder.append(messageFormatter.getSectionSeparator());
        chunkedStringBuilder.append(messageFormatter.getLineSeparator());
    }

}
