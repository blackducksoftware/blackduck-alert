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
package com.synopsys.integration.alert.web.custom;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.channel.issuetracker.IssueTrackerChannelKey;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.SelectCustomEndpoint;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

@Component
public class ProcessingSelectCustomEndpoint extends SelectCustomEndpoint {
    private final List<String> issueTrackerChannelKeys;

    @Autowired
    public ProcessingSelectCustomEndpoint(CustomEndpointManager customEndpointManager, ResponseFactory responseFactory, Gson gson, List<IssueTrackerChannelKey> issueTrackerChannelKeys) throws AlertException {
        super(ProviderDistributionUIConfig.KEY_PROCESSING_TYPE, customEndpointManager, responseFactory, gson);
        this.issueTrackerChannelKeys = issueTrackerChannelKeys.stream()
                                           .map(IssueTrackerChannelKey::getUniversalKey)
                                           .collect(Collectors.toList());
    }

    @Override
    protected List<LabelValueSelectOption> createData(FieldModel fieldModel) throws AlertException {
        String channelName = fieldModel.getFieldValue(ChannelDistributionUIConfig.KEY_CHANNEL_NAME).orElse("");
        return Arrays.stream(ProcessingType.values())
                   .filter(processingType -> this.shouldInclude(processingType, channelName))
                   .map(processingType -> new LabelValueSelectOption(processingType.getLabel(), processingType.name()))
                   .collect(Collectors.toList());
    }

    private boolean shouldInclude(ProcessingType processingType, String channelName) {
        // We do not want to expose the summary processing type as an option for issue tracker channels
        return !(issueTrackerChannelKeys.contains(channelName) && processingType == ProcessingType.SUMMARY);
    }
}
