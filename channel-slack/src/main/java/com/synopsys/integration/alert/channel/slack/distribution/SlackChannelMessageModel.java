/*
 * channel-slack
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.slack.distribution;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class SlackChannelMessageModel extends AlertSerializableModel {
    private final String markdownContent;

    public SlackChannelMessageModel(String markdownContent) {
        this.markdownContent = markdownContent;
    }

    public String getMarkdownContent() {
        return markdownContent;
    }

}
