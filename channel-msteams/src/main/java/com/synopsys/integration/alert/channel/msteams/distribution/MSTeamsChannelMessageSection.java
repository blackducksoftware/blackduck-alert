/*
 * channel-msteams
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.msteams.distribution;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

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
