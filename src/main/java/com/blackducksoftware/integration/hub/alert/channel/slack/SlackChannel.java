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

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.SlackConfigEntity;
import com.google.gson.Gson;

public class SlackChannel extends DistributionChannel<SlackEvent, SlackConfigEntity> {

    @Autowired
    public SlackChannel(final Gson gson) {
        super(gson, SlackEvent.class);
    }

    @Override
    public void sendMessage(final SlackEvent event, final SlackConfigEntity config) {
        // TODO Auto-generated method stub

    }

    @Override
    public void testMessage(final SlackEvent event, final SlackConfigEntity config) {
        // TODO Auto-generated method stub

    }

    @Override
    public void receiveMessage(final String message) {
        // TODO Auto-generated method stub

    }

}
