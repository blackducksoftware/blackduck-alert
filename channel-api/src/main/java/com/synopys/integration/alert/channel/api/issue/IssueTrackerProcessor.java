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

import java.io.Serializable;
import java.util.List;

import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopys.integration.alert.channel.api.issue.model.IssueTrackerModelHolder;

public class IssueTrackerProcessor<T extends Serializable> {
    private final IssueTrackerModelExtractor<T> modelExtractor;
    private final IssueTrackerMessageSender<T> messageSender;

    public IssueTrackerProcessor(IssueTrackerModelExtractor<T> modelExtractor, IssueTrackerMessageSender<T> messageSender) {
        this.modelExtractor = modelExtractor;
        this.messageSender = messageSender;
    }

    public final IssueTrackerResponse processMessages(ProviderMessageHolder messages) throws AlertException {
        List<IssueTrackerModelHolder<T>> channelMessages = modelExtractor.extractIssueTrackerModels(messages);
        return messageSender.sendMessages(channelMessages);
    }

}
