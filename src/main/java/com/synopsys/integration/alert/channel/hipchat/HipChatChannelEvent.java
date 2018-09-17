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
package com.synopsys.integration.alert.channel.hipchat;

import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.channel.event.ChannelEvent;

public class HipChatChannelEvent extends ChannelEvent {
    private final Integer roomId;
    private final Boolean notify;
    private final String color;

    public HipChatChannelEvent(final String createdAt, final String provider, final String notificationType, final String content, final Long notificationId, final Long commonConfigId, final Integer roomId, final Boolean notify,
        final String color) {
        super(EmailGroupChannel.COMPONENT_NAME, createdAt, provider, notificationType, content, notificationId, commonConfigId);
        this.roomId = roomId;
        this.notify = notify;
        this.color = color;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public Boolean getNotify() {
        return notify;
    }

    public String getColor() {
        return color;
    }
}
