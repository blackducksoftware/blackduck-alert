/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.alert.channel.hipchat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.google.gson.Gson;

@Component
public class HipChatChannel extends DistributionChannel<String> {
    private final static Logger logger = LoggerFactory.getLogger(HipChatChannel.class);
    private final Gson gson;

    @Autowired
    public HipChatChannel(final Gson gson) {
        this.gson = gson;
    }

    @JmsListener(destination = HipChatChannelConfig.CHANNEL_NAME)
    @Override
    public void recieveMessage(final String message) {
        logger.info("Received hipchat event message: {}", message);
        final HipChatEvent event = gson.fromJson(message, HipChatEvent.class);
        logger.info("HipChat event {}", event);
    }
}
