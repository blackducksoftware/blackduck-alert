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
package com.synopys.integration.alert.channel.api.issue;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.channel.message.ChunkedStringBuilder;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopys.integration.alert.channel.api.convert.SimpleMessageConverter;
import com.synopys.integration.alert.channel.api.issue.model.IssueCreationModel;

public class IssueTrackerSimpleMessageConverter {
    private final IssueTrackerMessageFormatter formatter;
    private final SimpleMessageConverter simpleMessageConverter;

    public IssueTrackerSimpleMessageConverter(IssueTrackerMessageFormatter formatter) {
        this.formatter = formatter;
        this.simpleMessageConverter = new SimpleMessageConverter(formatter);
    }

    public IssueCreationModel convertToIssueCreationModel(SimpleMessage simpleMessage) {
        LinkableItem provider = simpleMessage.getProvider();
        String rawTitle = String.format("%s[%s] | %s", provider.getLabel(), provider.getValue(), simpleMessage.getSummary());
        String truncatedTitle = StringUtils.truncate(rawTitle, formatter.getMaxTitleLength());

        String description;
        List<String> descriptionChunks = simpleMessageConverter.convertToFormattedMessageChunks(simpleMessage);
        if (descriptionChunks.size() > 0) {
            description = descriptionChunks.get(0);
        } else {
            description = "No description";
        }

        ChunkedStringBuilder descriptionCommentsBuilder = new ChunkedStringBuilder(formatter.getMaxCommentLength());
        for (int i = 1; i < descriptionChunks.size(); i++) {
            String descriptionChunk = descriptionChunks.get(i);
            descriptionCommentsBuilder.append(descriptionChunk);
        }

        List<String> descriptionComments = descriptionCommentsBuilder.collectCurrentChunks();

        return IssueCreationModel.simple(truncatedTitle, description, descriptionComments);
    }

}
