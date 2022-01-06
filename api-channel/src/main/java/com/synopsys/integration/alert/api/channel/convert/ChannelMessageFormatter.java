/*
 * api-channel
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.convert;

public abstract class ChannelMessageFormatter {
    public static final String DEFAULT_SECTION_SEPARATOR = "---------------------------------------";
    public static final String DEFAULT_NON_BREAKING_SPACE = " ";

    private final int maxMessageLength;
    private final String lineSeparator;
    private final String sectionSeparator;
    private final String nonBreakingSpace;

    public ChannelMessageFormatter(int maxMessageLength, String lineSeparator) {
        this(maxMessageLength, lineSeparator, DEFAULT_SECTION_SEPARATOR, DEFAULT_NON_BREAKING_SPACE);
    }

    public ChannelMessageFormatter(int maxMessageLength, String lineSeparator, String sectionSeparator, String nonBreakingSpace) {
        this.maxMessageLength = maxMessageLength;
        this.lineSeparator = lineSeparator;
        this.sectionSeparator = sectionSeparator;
        this.nonBreakingSpace = nonBreakingSpace;
    }

    public final int getMaxMessageLength() {
        return maxMessageLength;
    }

    public final String getLineSeparator() {
        return lineSeparator;
    }

    public final String getSectionSeparator() {
        return sectionSeparator + nonBreakingSpace;
    }

    public final String getNonBreakingSpace() {
        return nonBreakingSpace;
    }

    public abstract String encode(String txt);

    public abstract String emphasize(String txt);

    public abstract String createLink(String txt, String url);

}
