/*
 * api-channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.convert;

import com.synopsys.integration.alert.channel.api.convert.ChannelMessageFormatter;

public abstract class IssueTrackerMessageFormatter extends ChannelMessageFormatter {
    private final int maxTitleLength;
    private final int maxCommentLength;

    public IssueTrackerMessageFormatter(int maxTitleLength, int maxDescriptionLength, int maxCommentLength, String lineSeparator) {
        super(maxDescriptionLength, lineSeparator);
        this.maxTitleLength = maxTitleLength;
        this.maxCommentLength = maxCommentLength;
    }

    public IssueTrackerMessageFormatter(int maxTitleLength, int maxDescriptionLength, int maxCommentLength, String lineSeparator, String sectionSeparator, String nonBreakingSpace) {
        super(maxDescriptionLength, lineSeparator, sectionSeparator, nonBreakingSpace);
        this.maxTitleLength = maxTitleLength;
        this.maxCommentLength = maxCommentLength;
    }

    public int getMaxTitleLength() {
        return maxTitleLength;
    }

    public int getMaxCommentLength() {
        return maxCommentLength;
    }

    /**
     * Alias for getMaxMessageLength()
     * @return getMaxMessageLength()
     */
    public int getMaxDescriptionLength() {
        return getMaxMessageLength();
    }

}
