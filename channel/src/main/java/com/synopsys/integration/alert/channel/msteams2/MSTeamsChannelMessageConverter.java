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

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopys.integration.alert.channel.api.convert.AbstractChannelMessageConverter;

@Component
public class MSTeamsChannelMessageConverter extends AbstractChannelMessageConverter<MSTeamsJobDetailsModel, MSTeamsChannelMessageModel> {

    @Autowired
    protected MSTeamsChannelMessageConverter(MSTeamsChannelMessageFormatter channelMessageFormatter) {
        super(channelMessageFormatter);
    }

    @Override
    protected List<MSTeamsChannelMessageModel> convertSimpleMessageToChannelMessages(MSTeamsJobDetailsModel msTeamsJobDetailsModel, SimpleMessage simpleMessage, List<String> messageChunks) {
        return createMessageModel(messageChunks);
    }

    @Override
    protected List<MSTeamsChannelMessageModel> convertProjectMessageToChannelMessages(MSTeamsJobDetailsModel msTeamsJobDetailsModel, ProjectMessage projectMessage, List<String> messageChunks) {
        return createMessageModel(messageChunks);
    }

    private List<MSTeamsChannelMessageModel> createMessageModel(List<String> messageChunks) {
        // The title for each message is currently empty because the old implementation would attempt to build a title based on components, or default to an empty String.
        return messageChunks.stream()
                   .map(messageContent -> new MSTeamsChannelMessageModel(StringUtils.EMPTY, messageContent))
                   .collect(Collectors.toList());
    }
}
