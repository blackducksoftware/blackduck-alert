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
package com.synopsys.integration.alert.web.channel.model;

import java.util.List;

public class SlackDistributionConfig extends CommonDistributionConfig {
    private String webhook;
    private String channelUsername;
    private String channelName;

    public SlackDistributionConfig() {

    }

    public SlackDistributionConfig(final String id, final String webhook, final String channelUsername, final String channelName, final String distributionConfigId, final String distributionType, final String name,
        final String providerName, final String frequency, final String filterByProject, final String projectNamePattern, final List<String> configuredProjects, final List<String> notificationTypes, final String formatType) {
        super(id, distributionConfigId, distributionType, name, providerName, frequency, filterByProject, projectNamePattern, configuredProjects, notificationTypes, formatType);
        this.webhook = webhook;
        this.channelUsername = channelUsername;
        this.channelName = channelName;
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
