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
package com.blackducksoftware.integration.hub.alert.digest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;

public class DigestItemWriter implements ItemWriter<List<AbstractChannelEvent>> {
    private final static Logger logger = LoggerFactory.getLogger(DigestItemWriter.class);
    private final ChannelTemplateManager channelTemplateManager;

    @Autowired
    public DigestItemWriter(final ChannelTemplateManager channelTemplateManager) {
        this.channelTemplateManager = channelTemplateManager;
    }

    @Override
    public void write(final List<? extends List<AbstractChannelEvent>> eventList) throws Exception {
        try {
            logger.info("Digest Item Writer called");
            eventList.forEach(channelEventList -> {
                channelTemplateManager.sendEvents(channelEventList);
            });
        } catch (final Exception ex) {
            logger.error("Error occurred writing digest notification data to channels", ex);
        }
    }
}
