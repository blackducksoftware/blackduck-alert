/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.msteams.distribution;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class MSTeamsChannelMessageSection extends AlertSerializableModel {
    private final String sectionTitle;
    private final String content;

    public MSTeamsChannelMessageSection(String sectionTitle, String content) {
        this.sectionTitle = sectionTitle;
        this.content = content;
    }

    public String getTitle() {
        return sectionTitle;
    }

    public String getContent() {
        return content;
    }

}
