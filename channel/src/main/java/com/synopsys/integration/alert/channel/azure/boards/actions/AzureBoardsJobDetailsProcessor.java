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
package com.synopsys.integration.alert.channel.azure.boards.actions;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.processor.JobDetailsProcessor;

@Component
public class AzureBoardsJobDetailsProcessor extends JobDetailsProcessor {

    @Override
    protected DistributionJobDetailsModel convertToChannelJobDetails(Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return new AzureBoardsJobDetailsModel(
            extractFieldValue(AzureBoardsDescriptor.KEY_WORK_ITEM_COMMENT, configuredFieldsMap).map(Boolean::valueOf).orElse(false),
            extractFieldValueOrEmptyString(AzureBoardsDescriptor.KEY_AZURE_PROJECT, configuredFieldsMap),
            extractFieldValueOrEmptyString(AzureBoardsDescriptor.KEY_WORK_ITEM_TYPE, configuredFieldsMap),
            extractFieldValueOrEmptyString(AzureBoardsDescriptor.KEY_WORK_ITEM_COMPLETED_STATE, configuredFieldsMap),
            extractFieldValueOrEmptyString(AzureBoardsDescriptor.KEY_WORK_ITEM_REOPEN_STATE, configuredFieldsMap)
        );
    }
}
