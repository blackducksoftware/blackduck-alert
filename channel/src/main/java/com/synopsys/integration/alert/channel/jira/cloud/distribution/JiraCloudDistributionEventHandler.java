package com.synopsys.integration.alert.channel.jira.cloud.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.api.DistributionChannel;
import com.synopsys.integration.alert.channel.api.DistributionEventHandler;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;

@Component
public class JiraCloudDistributionEventHandler extends DistributionEventHandler<JiraCloudJobDetailsModel> {
    @Autowired
    public JiraCloudDistributionEventHandler(DistributionChannel<JiraCloudJobDetailsModel> channel,
        JobDetailsAccessor<JiraCloudJobDetailsModel> jobDetailsAccessor, ProcessingAuditAccessor auditAccessor) {
        super(channel, jobDetailsAccessor, auditAccessor);
    }

}
