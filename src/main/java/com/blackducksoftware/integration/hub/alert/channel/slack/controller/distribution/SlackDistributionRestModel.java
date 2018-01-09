/**
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.alert.channel.slack.controller.distribution;

import java.util.List;

import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

public class SlackDistributionRestModel extends CommonDistributionConfigRestModel {
    private static final long serialVersionUID = -3032738984577328749L;

    private String webhook;
    private String channelUsername;
    private String channelName;

    public SlackDistributionRestModel() {

    }

    public SlackDistributionRestModel(final String id, final String webhook, final String channelUsername, final String channelName, final String distributionConfigId, final String distributionType, final String name,
            final String frequency, final String filterByProject, final List<String> configuredProjects, final List<String> notificationTypes) {
        super(id, distributionConfigId, distributionType, name, frequency, filterByProject, configuredProjects, notificationTypes);
        this.webhook = webhook;
        this.channelUsername = channelUsername;
        this.channelName = channelName;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(final String webhook) {
        this.webhook = webhook;
    }

    public String getChannelUsername() {
        return channelUsername;
    }

    public void setChannelUsername(final String channelUsername) {
        this.channelUsername = channelUsername;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(final String channelName) {
        this.channelName = channelName;
    }

}
