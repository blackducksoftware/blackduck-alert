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
package com.blackducksoftware.integration.hub.alert.channel;

import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.email.EmailGroupEvent;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatEvent;
import com.blackducksoftware.integration.hub.alert.channel.slack.SlackEvent;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;

@Component
public class ChannelEventFactory {
    public AbstractChannelEvent createEvent(final Long id, final String distributionType, final ProjectData projectData) {
        switch (distributionType) {
        case SupportedChannels.EMAIL_GROUP:
            return new EmailGroupEvent(projectData, id);
        case SupportedChannels.HIPCHAT:
            return new HipChatEvent(projectData, id);
        case SupportedChannels.SLACK:
            return new SlackEvent(projectData, id);
        default:
            return new AbstractChannelEvent(projectData, id) {
                @Override
                public String getTopic() {
                    return distributionType;
                }
            };
        }
    }

}
