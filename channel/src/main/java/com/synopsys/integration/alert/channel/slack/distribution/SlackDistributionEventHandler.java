package com.synopsys.integration.alert.channel.slack.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.api.DistributionEventHandler;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.SlackJobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;

@Component
public class SlackDistributionEventHandler extends DistributionEventHandler<SlackJobDetailsModel> {
    @Autowired
    public SlackDistributionEventHandler(SlackChannel channel, SlackJobDetailsAccessor jobDetailsAccessor, ProcessingAuditAccessor auditAccessor) {
        super(channel, jobDetailsAccessor, auditAccessor);
    }

}
