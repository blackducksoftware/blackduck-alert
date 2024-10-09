package com.blackduck.integration.alert.channel.email.action;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.action.DistributionChannelMessageTestAction;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.descriptor.EmailChannelKey;
import com.blackduck.integration.alert.channel.email.distribution.EmailChannel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;

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
