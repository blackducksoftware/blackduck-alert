/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.action;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.processor.DistributionJobDetailsExtractor;
import com.synopsys.integration.alert.common.persistence.model.job.details.processor.DistributionJobFieldExtractor;
import com.synopsys.integration.alert.descriptor.api.EmailChannelKey;

@Component
public class EmailJobDetailsExtractor extends DistributionJobDetailsExtractor {
    private final DistributionJobFieldExtractor fieldExtractor;

    @Autowired
    public EmailJobDetailsExtractor(EmailChannelKey channelKey, DistributionJobFieldExtractor fieldExtractor) {
        super(channelKey);
        this.fieldExtractor = fieldExtractor;
    }

    @Override
    public EmailJobDetailsModel extractDetails(UUID jobId, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return new EmailJobDetailsModel(
            jobId,
            fieldExtractor.extractFieldValue(EmailDescriptor.KEY_SUBJECT_LINE, configuredFieldsMap).orElse(null),
            fieldExtractor.extractFieldValue(EmailDescriptor.KEY_PROJECT_OWNER_ONLY, configuredFieldsMap).map(Boolean::valueOf).orElse(false),
            fieldExtractor.extractFieldValue(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY, configuredFieldsMap).map(Boolean::valueOf).orElse(false),
            fieldExtractor.extractFieldValueOrEmptyString(EmailDescriptor.KEY_EMAIL_ATTACHMENT_FORMAT, configuredFieldsMap),
            fieldExtractor.extractFieldValues(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES, configuredFieldsMap)
        );
    }

}
