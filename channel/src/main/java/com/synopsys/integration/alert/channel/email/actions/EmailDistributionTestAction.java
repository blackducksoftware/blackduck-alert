/*
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
package com.synopsys.integration.alert.channel.email.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailChannel;
import com.synopsys.integration.alert.common.channel.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class EmailDistributionTestAction extends ChannelDistributionTestAction {
    private final EmailTestActionHelper emailTestActionHelper;

    @Autowired
    public EmailDistributionTestAction(EmailChannel emailChannel, EmailTestActionHelper emailTestActionHelper) {
        super(emailChannel);
        this.emailTestActionHelper = emailTestActionHelper;
    }

    @Override
    public MessageResult testConfig(
        DistributionJobModel testJobModel,
        @Nullable ConfigurationModel channelGlobalConfig,
        @Nullable String customTopic,
        @Nullable String customMessage,
        @Nullable String destination
    ) throws IntegrationException {
        DistributionJobModel updatedJobModel = creatUpdatedJobModelWithEmailAddresses(testJobModel, destination);
        return super.testConfig(updatedJobModel, channelGlobalConfig, customTopic, customMessage, destination);
    }

    private DistributionJobModel creatUpdatedJobModelWithEmailAddresses(DistributionJobModel originalJobModel, @Nullable String destination) throws IntegrationException {
        Set<String> updateEmailAddresses = emailTestActionHelper.createUpdatedEmailAddresses(originalJobModel, destination);
        EmailJobDetailsModel originalEmailJobDetails = originalJobModel.getDistributionJobDetails().getAs(DistributionJobDetailsModel.EMAIL);

        // For testing configuration, just use additional email addresses field
        List<String> originalAdditionalEmailAddresses = originalEmailJobDetails.getAdditionalEmailAddresses();
        List<String> additionalEmailAddressesToUse = new ArrayList<>(updateEmailAddresses.size() + originalAdditionalEmailAddresses.size());
        additionalEmailAddressesToUse.addAll(originalAdditionalEmailAddresses);
        additionalEmailAddressesToUse.addAll(updateEmailAddresses);

        EmailJobDetailsModel updatedEmailJobDetails = new EmailJobDetailsModel(
            originalEmailJobDetails.getSubjectLine(),
            false,
            true,
            originalEmailJobDetails.getAttachmentFileType(),
            additionalEmailAddressesToUse
        );
        return DistributionJobModel.builder()
                   .enabled(originalJobModel.isEnabled())
                   .name(originalJobModel.getName())
                   .distributionFrequency(originalJobModel.getDistributionFrequency())
                   .processingType(originalJobModel.getProcessingType())
                   .channelDescriptorName(originalJobModel.getChannelDescriptorName())
                   .createdAt(originalJobModel.getCreatedAt())
                   .lastUpdated(originalJobModel.getLastUpdated().orElse(null))
                   .blackDuckGlobalConfigId(originalJobModel.getBlackDuckGlobalConfigId())
                   .filterByProject(originalJobModel.isFilterByProject())
                   .projectNamePattern(originalJobModel.getProjectNamePattern().orElse(null))
                   .notificationTypes(originalJobModel.getNotificationTypes())
                   .projectFilterDetails(originalJobModel.getProjectFilterDetails())
                   .policyFilterPolicyNames(originalJobModel.getPolicyFilterPolicyNames())
                   .vulnerabilityFilterSeverityNames(originalJobModel.getVulnerabilityFilterSeverityNames())
                   .distributionJobDetails(updatedEmailJobDetails)
                   .build();
    }

}
