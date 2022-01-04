/*
 * channel-msteams
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.msteams.distribution;

import java.util.List;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

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
