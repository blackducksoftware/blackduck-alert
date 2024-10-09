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
