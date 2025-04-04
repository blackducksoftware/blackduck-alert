/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.convert;

import com.blackduck.integration.alert.api.channel.convert.ChannelMessageFormatter;

public abstract class IssueTrackerMessageFormatter extends ChannelMessageFormatter {
    private final int maxTitleLength;
    private final int maxCommentLength;

    protected IssueTrackerMessageFormatter(int maxTitleLength, int maxDescriptionLength, int maxCommentLength, String lineSeparator) {
        super(maxDescriptionLength, lineSeparator);
        this.maxTitleLength = maxTitleLength;
        this.maxCommentLength = maxCommentLength;
    }

    protected IssueTrackerMessageFormatter(
        int maxTitleLength,
        int maxDescriptionLength,
        int maxCommentLength,
        String lineSeparator,
        String sectionSeparator,
        String nonBreakingSpace
    ) {
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
