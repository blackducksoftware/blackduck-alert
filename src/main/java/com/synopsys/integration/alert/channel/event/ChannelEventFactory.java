/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.channel.event;

import java.util.Date;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.rest.RestConstants;

@Transactional
@Component
public class ChannelEventFactory {
    private final ContentConverter contentConverter;

    @Autowired
    public ChannelEventFactory(final ContentConverter contentConverter) {
        this.contentConverter = contentConverter;
    }

    public ChannelEvent createChannelEvent(final Long commonDistributionConfigId, final String destination, final NotificationContent notificationContent) {
        return new ChannelEvent(destination, RestConstants.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(), notificationContent.getContent(),
                commonDistributionConfigId, notificationContent.getId());
    }

    public ChannelEvent createChannelTestEvent(final String destination) {
        final NotificationContent testContent = new NotificationContent(new Date(), "Alert", "Test Message", "Alert has sent this test message");
        return createChannelEvent(null, destination, testContent);
    }

}
