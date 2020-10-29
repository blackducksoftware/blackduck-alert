/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.persistence.model.job.details;

import com.synopsys.integration.alert.common.channel.key.ChannelKey;

public abstract class DistributionJobDetailsModel {
    private final String channelDescriptorName;

    // TODO create a new sub-project for descriptors (or at least descriptor keys)
    /* package private */ DistributionJobDetailsModel(String channelDescriptorName) {
        this.channelDescriptorName = channelDescriptorName;
    }

    public DistributionJobDetailsModel(ChannelKey channelKey) {
        this(channelKey.getUniversalKey());
    }

    public boolean isAzureBoardsDetails() {
        return isChannelDetails("channel_azure_boards");
    }

    public AzureBoardsJobDetailsModel getAsAzureBoardsJobDetails() {
        return (AzureBoardsJobDetailsModel) this;
    }

    public boolean isEmailDetails() {
        return isChannelDetails("channel_email");
    }

    public EmailJobDetailsModel getAsEmailJobDetails() {
        return (EmailJobDetailsModel) this;
    }

    public boolean isJiraCloudDetails() {
        return isChannelDetails("channel_jira_cloud");
    }

    public JiraCloudJobDetailsModel getAsJiraCouldJobDetails() {
        return (JiraCloudJobDetailsModel) this;
    }

    public boolean isJiraServerDetails() {
        return isChannelDetails("channel_jira_server");
    }

    public JiraServerJobDetailsModel getAsJiraServerJobDetails() {
        return (JiraServerJobDetailsModel) this;
    }

    public boolean isMSTeamsDetails() {
        return isChannelDetails("msteamskey");
    }

    public MSTeamsJobDetailsModel getAsMSTeamsJobDetails() {
        return (MSTeamsJobDetailsModel) this;
    }

    public boolean isSlackDetails() {
        return isChannelDetails("channel_slack");
    }

    public SlackJobDetailsModel getAsSlackJobDetails() {
        return (SlackJobDetailsModel) this;
    }

    private boolean isChannelDetails(String channelDescriptorName) {
        return this.channelDescriptorName.equals(channelDescriptorName);
    }

}
