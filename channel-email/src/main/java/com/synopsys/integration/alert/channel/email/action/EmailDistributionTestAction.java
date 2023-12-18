/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.action;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.action.DistributionChannelMessageTestAction;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.email.distribution.EmailChannel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.api.descriptor.EmailChannelKey;

@Component
public class EmailDistributionTestAction extends DistributionChannelMessageTestAction<EmailJobDetailsModel> {
    private final EmailTestActionHelper emailTestActionHelper;

    @Autowired
    public EmailDistributionTestAction(EmailChannelKey channelKey, EmailChannel distributionChannel, EmailTestActionHelper emailTestActionHelper) {
        super(channelKey, distributionChannel);
        this.emailTestActionHelper = emailTestActionHelper;
    }

    @Override
    protected EmailJobDetailsModel resolveTestDistributionDetails(DistributionJobModel testJobModel) throws AlertException {
        List<String> updateEmailAddresses = new ArrayList<>(emailTestActionHelper.createUpdatedEmailAddresses(testJobModel));
        EmailJobDetailsModel originalEmailJobDetails = testJobModel.getDistributionJobDetails().getAs(DistributionJobDetailsModel.EMAIL);

        return new EmailJobDetailsModel(
            testJobModel.getJobId(),
            originalEmailJobDetails.getSubjectLine().orElse(null),
            false,
            true,
            originalEmailJobDetails.getAttachmentFileType(),
            updateEmailAddresses
        );
    }

}
