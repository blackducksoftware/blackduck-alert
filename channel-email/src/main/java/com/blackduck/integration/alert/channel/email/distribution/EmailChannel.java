/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.MessageBoardChannel;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;

@Component
public class EmailChannel extends MessageBoardChannel<EmailJobDetailsModel, EmailChannelMessageModel> {
    @Autowired
    public EmailChannel(
        EmailChannelMessageConverter emailChannelMessageConverter,
        EmailChannelMessageSender emailChannelMessageSender,
        EventManager eventManager,
        ExecutingJobManager executingJobManager
    ) {
        super(emailChannelMessageConverter, emailChannelMessageSender, eventManager, executingJobManager);
    }

}
