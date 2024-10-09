/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.slack.distribution;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class SlackChannelMessageModel extends AlertSerializableModel {
    private final String markdownContent;

    public SlackChannelMessageModel(String markdownContent) {
        this.markdownContent = markdownContent;
    }

    public String getMarkdownContent() {
        return markdownContent;
    }

}
