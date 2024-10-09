/*
 * channel-msteams
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.msteams.actions;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.channel.msteams.descriptor.MsTeamsDescriptor;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.processor.DistributionJobDetailsExtractor;
import com.blackduck.integration.alert.common.persistence.model.job.details.processor.DistributionJobFieldExtractor;
import com.blackduck.integration.alert.api.descriptor.MsTeamsKey;

@Component
public class MsTeamsJobDetailsExtractor extends DistributionJobDetailsExtractor {
    private final DistributionJobFieldExtractor fieldExtractor;

    @Autowired
    public MsTeamsJobDetailsExtractor(MsTeamsKey channelKey, DistributionJobFieldExtractor fieldExtractor) {
        super(channelKey);
        this.fieldExtractor = fieldExtractor;
    }

    @Override
    public DistributionJobDetailsModel extractDetails(UUID jobId, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return new MSTeamsJobDetailsModel(jobId, fieldExtractor.extractFieldValueOrEmptyString(MsTeamsDescriptor.KEY_WEBHOOK, configuredFieldsMap));
    }

}
