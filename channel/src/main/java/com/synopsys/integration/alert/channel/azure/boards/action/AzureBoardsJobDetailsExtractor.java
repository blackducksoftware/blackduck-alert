/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.action;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.processor.JobDetailsExtractor;

@Component
public class AzureBoardsJobDetailsExtractor extends JobDetailsExtractor {
    @Override
    protected DistributionJobDetailsModel convertToChannelJobDetails(UUID jobId, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return new AzureBoardsJobDetailsModel(
            jobId,
            extractFieldValue(AzureBoardsDescriptor.KEY_WORK_ITEM_COMMENT, configuredFieldsMap).map(Boolean::valueOf).orElse(false),
            extractFieldValueOrEmptyString(AzureBoardsDescriptor.KEY_AZURE_PROJECT, configuredFieldsMap),
            extractFieldValueOrEmptyString(AzureBoardsDescriptor.KEY_WORK_ITEM_TYPE, configuredFieldsMap),
            extractFieldValueOrEmptyString(AzureBoardsDescriptor.KEY_WORK_ITEM_COMPLETED_STATE, configuredFieldsMap),
            extractFieldValueOrEmptyString(AzureBoardsDescriptor.KEY_WORK_ITEM_REOPEN_STATE, configuredFieldsMap)
        );
    }

}
