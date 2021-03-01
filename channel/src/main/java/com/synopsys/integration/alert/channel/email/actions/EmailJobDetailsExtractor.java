/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.actions;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.processor.JobDetailsExtractor;

@Component
public class EmailJobDetailsExtractor extends JobDetailsExtractor {
    @Override
    protected DistributionJobDetailsModel convertToChannelJobDetails(UUID jobId, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return new EmailJobDetailsModel(
            jobId,
            extractFieldValueOrEmptyString(EmailDescriptor.KEY_SUBJECT_LINE, configuredFieldsMap),
            extractFieldValue(EmailDescriptor.KEY_PROJECT_OWNER_ONLY, configuredFieldsMap).map(Boolean::valueOf).orElse(false),
            extractFieldValue(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY, configuredFieldsMap).map(Boolean::valueOf).orElse(false),
            extractFieldValueOrEmptyString(EmailDescriptor.KEY_EMAIL_ATTACHMENT_FORMAT, configuredFieldsMap),
            extractFieldValues(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES, configuredFieldsMap)
        );
    }

}
