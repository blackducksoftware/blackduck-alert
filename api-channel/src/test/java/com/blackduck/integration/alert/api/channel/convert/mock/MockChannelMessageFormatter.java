/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.convert.mock;

import com.blackduck.integration.alert.api.channel.convert.ChannelMessageFormatter;

public class MockChannelMessageFormatter extends ChannelMessageFormatter {
    public MockChannelMessageFormatter(int maxMessageLength) {
        super(maxMessageLength, System.lineSeparator());
    }

    @Override
    public String encode(String txt) {
        return "<b>" + txt + "</b>";
    }

    @Override
    public String emphasize(String txt) {
        return "<!>" + txt + "</!>";
    }

    @Override
    public String createLink(String txt, String url) {
        return "<ln>" + txt + " - " + url + "</ln>";
    }

}
