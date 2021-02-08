package com.synopsys.integration.alert.channel.slack2;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class SlackChannelMessageModel extends AlertSerializableModel {
    private final String content;

    public SlackChannelMessageModel(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
