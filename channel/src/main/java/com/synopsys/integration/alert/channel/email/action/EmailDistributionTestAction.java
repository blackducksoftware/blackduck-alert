/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.api.action.DistributionChannelTestAction;
import com.synopsys.integration.alert.channel.email.distribution.EmailChannelV2;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;

@Component
public class EmailDistributionTestAction extends DistributionChannelTestAction<EmailJobDetailsModel> {
    private final EmailTestActionHelper emailTestActionHelper;

    @Autowired
    public EmailDistributionTestAction(EmailChannelV2 distributionChannel, EmailTestActionHelper emailTestActionHelper) {
        super(distributionChannel);
        this.emailTestActionHelper = emailTestActionHelper;
    }

    @Override
    protected EmailJobDetailsModel resolveTestDistributionDetails(DistributionJobModel testJobModel, @Nullable String destination) throws AlertException {
        Set<String> updateEmailAddresses = emailTestActionHelper.createUpdatedEmailAddresses(testJobModel, destination);
        EmailJobDetailsModel originalEmailJobDetails = testJobModel.getDistributionJobDetails().getAs(DistributionJobDetailsModel.EMAIL);

        // For testing configuration, just use additional email addresses field
        List<String> originalAdditionalEmailAddresses = originalEmailJobDetails.getAdditionalEmailAddresses();
        List<String> additionalEmailAddressesToUse = new ArrayList<>(updateEmailAddresses.size() + originalAdditionalEmailAddresses.size());
        additionalEmailAddressesToUse.addAll(originalAdditionalEmailAddresses);
        additionalEmailAddressesToUse.addAll(updateEmailAddresses);

        return new EmailJobDetailsModel(
            testJobModel.getJobId(),
            originalEmailJobDetails.getSubjectLine(),
            false,
            true,
            originalEmailJobDetails.getAttachmentFileType(),
            additionalEmailAddressesToUse
        );
    }

}
