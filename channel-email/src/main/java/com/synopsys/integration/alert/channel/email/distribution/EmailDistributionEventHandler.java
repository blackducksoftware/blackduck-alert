package com.synopsys.integration.alert.channel.email.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.api.DistributionEventHandler;
import com.synopsys.integration.alert.common.persistence.accessor.EmailJobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;

@Component
public class EmailDistributionEventHandler extends DistributionEventHandler<EmailJobDetailsModel> {
    @Autowired
    public EmailDistributionEventHandler(EmailChannel channel, EmailJobDetailsAccessor jobDetailsAccessor, ProcessingAuditAccessor auditAccessor) {
        super(channel, jobDetailsAccessor, auditAccessor);
    }

}
