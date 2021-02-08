package com.synopsys.integration.alert.channel.slack2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopys.integration.alert.channel.api.DistributionEventReceiver;

@Component
public class SlackDistributionEventReceiver extends DistributionEventReceiver<SlackJobDetailsModel> {
    @Autowired
    public SlackDistributionEventReceiver(Gson gson, AuditAccessor auditAccessor, JobDetailsAccessor jobDetailsAccessor, SlackChannelV2 channel) {
        super(gson, auditAccessor, jobDetailsAccessor, channel);
    }

}
