package com.synopsys.integration.alert.channel.msteams2;

public class MSTeamsChannelMessageSection {
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
