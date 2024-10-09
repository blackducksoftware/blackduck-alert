package com.blackduck.integration.alert.channel.email.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.DistributionEventHandler;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.common.persistence.accessor.EmailJobDetailsAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;

@Component
public class EmailDistributionEventHandler extends DistributionEventHandler<EmailJobDetailsModel> {
    @Autowired
    public EmailDistributionEventHandler(EmailChannel channel, EmailJobDetailsAccessor jobDetailsAccessor, EventManager eventManager) {
        super(channel, jobDetailsAccessor, eventManager);
    }

}
