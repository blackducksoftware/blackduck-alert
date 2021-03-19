/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.api.MessageBoardChannel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;

@Component
public class EmailChannelV2 extends MessageBoardChannel<EmailJobDetailsModel, EmailChannelMessageModel> {
    @Autowired
    public EmailChannelV2(EmailChannelMessageConverter emailChannelMessageConverter, EmailChannelMessageSender emailChannelMessageSender) {
        super(emailChannelMessageConverter, emailChannelMessageSender);
    }

}
