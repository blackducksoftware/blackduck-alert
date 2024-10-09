/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.action;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.blackduck.integration.alert.api.descriptor.AzureBoardsChannelKey;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.processor.DistributionJobDetailsExtractor;
import com.blackduck.integration.alert.common.persistence.model.job.details.processor.DistributionJobFieldExtractor;

@Component
public class AzureBoardsJobDetailsExtractor extends DistributionJobDetailsExtractor {
    private final DistributionJobFieldExtractor fieldExtractor;

    @Autowired
    public AzureBoardsJobDetailsExtractor(AzureBoardsChannelKey channelKey, DistributionJobFieldExtractor fieldExtractor) {
        super(channelKey);
        this.fieldExtractor = fieldExtractor;
    }

    @Override
    public AzureBoardsJobDetailsModel extractDetails(UUID jobId, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return new AzureBoardsJobDetailsModel(
            jobId,
            fieldExtractor.extractFieldValue(AzureBoardsDescriptor.KEY_WORK_ITEM_COMMENT, configuredFieldsMap).map(Boolean::valueOf).orElse(false),
            fieldExtractor.extractFieldValueOrEmptyString(AzureBoardsDescriptor.KEY_AZURE_PROJECT, configuredFieldsMap),
            fieldExtractor.extractFieldValueOrEmptyString(AzureBoardsDescriptor.KEY_WORK_ITEM_TYPE, configuredFieldsMap),
            fieldExtractor.extractFieldValueOrEmptyString(AzureBoardsDescriptor.KEY_WORK_ITEM_COMPLETED_STATE, configuredFieldsMap),
            fieldExtractor.extractFieldValueOrEmptyString(AzureBoardsDescriptor.KEY_WORK_ITEM_REOPEN_STATE, configuredFieldsMap)
        );
    }

}
