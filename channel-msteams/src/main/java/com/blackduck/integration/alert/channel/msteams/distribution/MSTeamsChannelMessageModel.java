/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.msteams.distribution;

import java.util.List;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class MSTeamsChannelMessageModel extends AlertSerializableModel {
    private final String messageTitle;
    private final List<MSTeamsChannelMessageSection> messageSections;

    public MSTeamsChannelMessageModel(String messageTitle, List<MSTeamsChannelMessageSection> messageSections) {
        this.messageTitle = messageTitle;
        this.messageSections = messageSections;
    }

    public String getTitle() {
        return messageTitle;
    }

    public List<MSTeamsChannelMessageSection> getSections() {
        return messageSections;
    }

}
