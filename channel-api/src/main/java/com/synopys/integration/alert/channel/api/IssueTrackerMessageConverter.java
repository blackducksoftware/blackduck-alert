/**
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
package com.synopys.integration.alert.channel.api;

import java.util.LinkedList;
import java.util.List;

import com.synopsys.integration.alert.processor.api.detail.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

public abstract class IssueTrackerMessageConverter<T> implements ChannelMessageConverter<T> {
    private final IssueTrackerMessageResolver<T> issueTrackerMessageResolver;

    public IssueTrackerMessageConverter(IssueTrackerMessageResolver<T> issueTrackerMessageResolver) {
        this.issueTrackerMessageResolver = issueTrackerMessageResolver;
    }

    @Override
    public List<T> convertToChannelMessages(ProviderMessageHolder messages) {
        List<T> issueTrackerChannelMessages = new LinkedList<>();

        for (SimpleMessage simpleMessage : messages.getSimpleMessages()) {
            T resolvedMessage = issueTrackerMessageResolver.resolve(simpleMessage);
            issueTrackerChannelMessages.add(resolvedMessage);
        }

        for (ProjectMessage projectMessage : messages.getProjectMessages()) {
            T resolvedMessage = issueTrackerMessageResolver.resolve(projectMessage);
            issueTrackerChannelMessages.add(resolvedMessage);
        }

        return issueTrackerChannelMessages;
    }

}
