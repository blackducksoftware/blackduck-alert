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
