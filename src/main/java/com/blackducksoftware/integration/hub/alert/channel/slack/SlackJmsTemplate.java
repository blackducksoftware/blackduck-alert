/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.channel.slack;

import javax.jms.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.alert.AbstractJmsTemplate;
import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;

public class SlackJmsTemplate extends AbstractJmsTemplate {

    @Autowired
    public SlackJmsTemplate(final ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public String getDestinationName() {
        return SupportedChannels.SLACK;
    }

}
