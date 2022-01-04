/*
 * channel-slack
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.slack.action;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.processor.DistributionJobDetailsExtractor;
import com.synopsys.integration.alert.common.persistence.model.job.details.processor.DistributionJobFieldExtractor;
import com.synopsys.integration.alert.descriptor.api.SlackChannelKey;

@Component
public class SlackJobDetailsExtractor extends DistributionJobDetailsExtractor {
    private final DistributionJobFieldExtractor fieldExtractor;

    @Autowired
    public SlackJobDetailsExtractor(SlackChannelKey channelKey, DistributionJobFieldExtractor fieldExtractor) {
        super(channelKey);
        this.fieldExtractor = fieldExtractor;
    }

    @Override
    public DistributionJobDetailsModel extractDetails(UUID jobId, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return new SlackJobDetailsModel(
            jobId,
            fieldExtractor.extractFieldValueOrEmptyString(SlackDescriptor.KEY_WEBHOOK, configuredFieldsMap),
            fieldExtractor.extractFieldValueOrEmptyString(SlackDescriptor.KEY_CHANNEL_NAME, configuredFieldsMap),
            fieldExtractor.extractFieldValueOrEmptyString(SlackDescriptor.KEY_CHANNEL_USERNAME, configuredFieldsMap)
        );
    }

}
